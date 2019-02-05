package com.aem.sumanta.postprocess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.aem.sumanta.Utils.SPARQL;
import com.aem.sumanta.Utils.Utility;
import com.aem.sumanta.dbpedia.NameOccurrence;
import com.aem.sumanta.dbpedia.ontology.UriRef;
import com.aem.sumanta.langdect.DetectLanguage;
import com.aem.sumanta.nlp.ContentItem;
import com.aem.sumanta.nlp.OpenNlp;

/**
 * @author Sumanta Pakira
 *
 */
@Component
@Service
public class TextProcessor implements SlingPostProcessor {

	private static final Logger log = LoggerFactory
			.getLogger(TextProcessor.class);
	protected static final String LIBS_RESOURCE_TYPE = "foundation/components/text";
	protected static final String PERSON_NAMESPACE = "person";
	protected static final String LOCATION_NAMESPACE = "location";
	protected static final String ORGANIZATION_NAMESPACE = "organization";
	protected static final String TAGS_PATH = "/etc/tags/";

	private String restrictToNamespace = "semantics-content";

	@Reference
	private OpenNlp nlp;

	/* (non-Javadoc)
	 * @see org.apache.sling.servlets.post.SlingPostProcessor#process(org.apache.sling.api.SlingHttpServletRequest, java.util.List)
	 */
	@Override
	public void process(SlingHttpServletRequest request, List<Modification> arg1) {

		final Resource textResource = request.getResource();
		long initTime = System.currentTimeMillis();
		try {
			log.info(" Resource type is {} ", textResource.getResourceType());

			if (textResource.isResourceType(LIBS_RESOURCE_TYPE)) {

				Session session = request.getResourceResolver().adaptTo(
						Session.class);
				String text = session.getProperty(
						textResource.getPath() + "/text").getString();
				ContentItem content = new ContentItem();

				if (!StringUtils.isBlank(text)) {
					content.setStream(new ByteArrayInputStream(text.getBytes()));
				} else {
					return;
				}

				DetectLanguage langIdentifier = new DetectLanguage();

				log.info("Detected language is {} ",
						langIdentifier.getLanguage(text));

				long languageDetenctionTimeTaken = System.currentTimeMillis()
						- initTime;
				log.info("Time taken to detect language is {} ",
						languageDetenctionTimeTaken / 1000);

				content.setLanguage(langIdentifier.getLanguage(text));
				String utf8text = Utility.removeNonUtf8CompliantCharacters(text);
				content.setText(utf8text.replaceAll("\'", ""));

				Map<String, List<NameOccurrence>> parsonEntityNames = extractPersonNameOccurrences(content);
				Map<String, List<NameOccurrence>> placeEntityNames = extractLocationNameOccurrences(content);
				Map<String, List<NameOccurrence>> orgEntityNames = extractOrganizationNameOccurrences(content);

				HashMap<String, String> personsMap = null;
				if (parsonEntityNames.size() > 0) {
					personsMap = SPARQL.executeQuery(content.getLanguage(),
							parsonEntityNames);
				} else {
					log.info("Text Processor did not find any Entity for Person.");
				}

				HashMap<String, String> locationsMap = null;
				if (placeEntityNames.size() > 0) {
					locationsMap = SPARQL.executeQuery(content.getLanguage(),
							placeEntityNames);
				}
				{
					log.info("Text Processor did not find any Entity for Place.");
				}

				HashMap<String, String> orgsMap = null;
				if (orgEntityNames.size() > 0) {
					orgsMap = SPARQL.executeQuery(content.getLanguage(),
							orgEntityNames);
				}
				{
					log.info("Text Processor did not find any Entity for Organization.");
				}

				final TagManager tagManager = request.getResourceResolver()
						.adaptTo(TagManager.class);

				Tag namespace = null;

				if (StringUtils.isNotEmpty(this.restrictToNamespace)) {
					for (final Tag potentialNamespace : tagManager
							.getNamespaces()) {
						if (this.restrictToNamespace.equals(potentialNamespace
								.getName())) {
							namespace = potentialNamespace;
							break;
						}
					}
					if (namespace == null) {
						log.warn(
								"Failed to resolve namespace the post processor is restricted to: {}",
								this.restrictToNamespace);
						return;
					}
				}

				@SuppressWarnings("unchecked")
				List<String> marged = ListUtils.union(
						createOrUpdatesTags(tagManager, personsMap,
								PERSON_NAMESPACE),
						createOrUpdatesTags(tagManager, locationsMap,
								LOCATION_NAMESPACE));

				@SuppressWarnings("unchecked")
				List<String> finalmarged = ListUtils.union(
						marged,
						createOrUpdatesTags(tagManager, orgsMap,
								ORGANIZATION_NAMESPACE));

				updatePageTags(finalmarged, request.getResource());

				getContentItemText(personsMap, content);
				getContentItemText(locationsMap, content);
				String updatedtext = getContentItemText(orgsMap, content);

				final ModifiableValueMap properties = request.getResource()
						.adaptTo(ModifiableValueMap.class);
				if (properties == null) {
					log.error(
							"Cannot adapt resource {} to a ModifiableValueMap. Possible permissions error.",
							request.getResource().getPath());
					return;
				} else {
					log.info("Setting up the text as {} ", updatedtext);
					properties.put("text", updatedtext);
				}

				long totaltimeTaken = System.currentTimeMillis() - initTime;
				log.info("Total time taken {} ", totaltimeTaken);

			} else {
				return;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();

		}

	}

	/**
	 * @param list
	 * @param resource
	 */
	private void updatePageTags(List<String> list, Resource resource) {

		try {
			if (list != null) {
				Node currentnode = resource.adaptTo(Node.class);
				Node pageContentNode = currentnode.getParent().getParent();
				Session session = pageContentNode.getSession();
				ValueFactory valueFactory = session.getValueFactory();
				Value[] value = new Value[list.size()];

				if (pageContentNode.getPrimaryNodeType().isNodeType(
						"cq:PageContent")) {

					for (int i = 0; i < list.size(); i++) {
						log.info("Tags values are {} ", list.get(i));
						value[i] = valueFactory.createValue(list.get(i));
					}

					pageContentNode.setProperty("cq:tags", value);
					session.save();
				}
			} else {
				log.error("Cannot update page with Tags because List is null");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * @param map
	 * @param ci
	 * @return
	 */
	private String getContentItemText(HashMap<String, String> map,
			ContentItem ci) {

		if (map != null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				log.info("Key Found {}", key);
				if (ci.getText().contains(key)) {
					ci.setText((ci.getText().replaceAll(key, entry.getValue())));
				}
			}
		} else {
			log.info("Cannot process text, Map is null.");
		}
		return ci.getText();
	}

	/**
	 * @param tgMgr
	 * @param map
	 * @param type
	 * @return
	 * @throws AccessControlException
	 * @throws InvalidTagFormatException
	 * @throws RepositoryException
	 */
	public ArrayList<String> createOrUpdatesTags(TagManager tgMgr,
			HashMap<String, String> map, String type)
			throws AccessControlException, InvalidTagFormatException,
			RepositoryException {

		ArrayList<String> tagList = new ArrayList<String>();
		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				String tagNameSpace = TAGS_PATH + restrictToNamespace + "/"
						+ type + "/" + key.replaceAll(" ", "_");
				if (tgMgr.resolve(tagNameSpace) == null) {
					Tag tagId = tgMgr.createTag(tagNameSpace, key, value);
					tagList.add(tagId.getTagID());
				} else {
					tagList.add(tgMgr.resolve(tagNameSpace).getTagID());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tagList;
	}

	public Map<String, List<NameOccurrence>> extractPersonNameOccurrences(
			ContentItem ci) throws InvalidFormatException, IOException {
		return extractNameOccurrences(
				nlp.getNameModel(PERSON_NAMESPACE, ci.getLanguage()), ci);
	}

	public Map<String, List<NameOccurrence>> extractLocationNameOccurrences(
			ContentItem ci) throws InvalidFormatException, IOException {
		return extractNameOccurrences(
				nlp.getNameModel(LOCATION_NAMESPACE, ci.getLanguage()), ci);
	}

	public Map<String, List<NameOccurrence>> extractOrganizationNameOccurrences(
			ContentItem ci) throws InvalidFormatException, IOException {
		return extractNameOccurrences(
				nlp.getNameModel(ORGANIZATION_NAMESPACE, ci.getLanguage()), ci);
	}

	/**
	 * @param nameFinderModel
	 * @param ci
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	protected Map<String, List<NameOccurrence>> extractNameOccurrences(
			TokenNameFinderModel nameFinderModel, ContentItem ci)
			throws InvalidFormatException, IOException {
		
		SentenceDetector sentenceDetector = nlp.getSentenceDetector(ci
				.getLanguage());
		Tokenizer tokenizer = nlp.getTokenizer(ci.getLanguage());

		Map<String, List<NameOccurrence>> nameOccurrences = new LinkedHashMap<String, List<NameOccurrence>>();
		NameFinderME nameFinder = new NameFinderME(nameFinderModel);

		Span[] tokenSpans = tokenizer.tokenizePos(ci.getText());
		String[] tokens = Span.spansToStrings(tokenSpans, ci.getText());
		Span[] nameSpans = nameFinder.find(tokens);

		String name = StringUtils.EMPTY;

		for (int i = 0; i < nameSpans.length; i++) {
			name = ci.getText().substring(
					tokenSpans[nameSpans[i].getStart()].getStart(),
					tokenSpans[nameSpans[i].getEnd() - 1].getEnd());
			log.info("Name is {} ", name);
			log.info("Type is {} ", nameSpans[i].getType());
			NameOccurrence occurrence = new NameOccurrence(name, new UriRef(
					nameSpans[i].getType()), tokens[nameSpans[i].getStart()]);

			List<NameOccurrence> occurrences = nameOccurrences.get(name);
			if (occurrences == null) {
				occurrences = new ArrayList<NameOccurrence>();
				occurrences.add(occurrence);
				nameOccurrences.put(name, occurrences);
			}

		}
		return nameOccurrences;
	}

}
