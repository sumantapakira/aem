package com.aem.sumanta.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.aem.sumanta.dbpedia.NameOccurrence;
import com.aem.sumanta.dbpedia.ontology.UriRef;

/**
 * @author Sumanta Pakira
 *
 */
public class SPARQL {

	private static final Logger log = LoggerFactory.getLogger(SPARQL.class);
	private final static String DBPEDIA_END_POINT = "http://dbpedia.org/sparql";
	private static String WIKI_PEDIA_URI = ".wikipedia.org/wiki/";
	private static String protocol = "http://";

	/**
	 * @param lang
	 * @param entityNames
	 * @return
	 */
	public static HashMap<String, String> executeQuery(String lang,
			Map<String, List<NameOccurrence>> entityNames) {
		StringBuilder sb = new StringBuilder();
		HashMap<String, String> map = new HashMap<String, String>();

		sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		sb.append("SELECT ?resource");
		sb.append(" WHERE  { ");

		for (Map.Entry<String, List<NameOccurrence>> nameInContext : entityNames
				.entrySet()) {
			String name = nameInContext.getKey();
			List<NameOccurrence> occurrences = nameInContext.getValue();

			for (NameOccurrence occurrence : occurrences) {
				UriRef type = occurrence.getType();
				if (sb.toString().toLowerCase()
						.indexOf(type.getUnicodeString().toLowerCase()) != -1) {
					// sb.append("?resource a <"+type.getUnicodeString()+"> .");
					sb.append(" UNION{ ");
					sb.append("?resource rdfs:label '" + name + "'@" + lang
							+ ".} ");
				} else {
					sb.append("?resource a <" + type.getUnicodeString() + "> .");
					sb.append(" {?resource rdfs:label '" + name + "'@" + lang
							+ ".}");
				}
			}
		}
		sb.append("}");

		log.info("Query is {} ", sb.toString());
		Query query = QueryFactory.create(sb.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				DBPEDIA_END_POINT, query);
		ResultSet results = qexec.execSelect();

		while (results.hasNext()) {
			QuerySolution qs = results.next();
			RDFNode s = qs.get("resource");
			if (s.isResource()) {
				String uri = s.asResource().getURI();
				log.info("Query result 1 : " + uri);
				log.info("Query result 2 : " + s.asResource().getLocalName());
				String localName = s.asResource().getLocalName();

				StringBuilder wikisb = new StringBuilder();

				wikisb.append(protocol);
				wikisb.append(lang);
				wikisb.append(WIKI_PEDIA_URI);
				wikisb.append(localName);

				String htmlAttrbute = "<a href ='" + wikisb.toString()
						+ "' target='_blank'>" + localName.replace("_", " ")
						+ "</a>";
				map.put(localName.replace("_", " "), htmlAttrbute);

			}
		}
		return map;
	}

}
