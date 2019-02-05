package com.aem.sumanta.nlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.sumanta.fileprovider.BundleDataFileProvider;
import com.aem.sumanta.fileprovider.FileProvider;



/**
 * @author Sumanta Pakira
 *
 */
@Component(immediate=true)
@Service(value=OpenNlp.class)
public class OpenNlp {

	private static final Logger log = LoggerFactory.getLogger(OpenNlp.class);
	
	@Reference
	FileProvider bdfp;
	
	 /**
	 * @param language
	 * @return
	 */
	@SuppressWarnings("unused")
	public SentenceDetector getSentenceDetector(String language) {
	        SentenceModel model =null;
	        String modelName =null;
	        if(modelName == null){
	            try {
	                model = getSentenceModel(language);
	            } catch (Exception e) {
	                log.error("Unable to load default Sentence Detection model for language '"+language+"'!",e);
	                return null;
	            }
	        } else {
	            try {
	                model = getModel(SentenceModel.class, modelName, null);
	            } catch (Exception e) {
	                log.warn("Unable to load Sentence Detection model for language '"
	                        +language+"' from the configured model '"+modelName+"'!",e);
	                return null;
	            }
	        }
	        if(model != null) {
	            log.info("Sentence Detection Model {} for lanugage '{}' version: {}",
	                new Object[]{model.getClass().getSimpleName(), 
	                             model.getLanguage(), 
	                             model.getVersion() != null ? model.getVersion() : "undefined"});
	            return new SentenceDetectorME(model);
	        }else{
	        	log.error("Model cannot be null!");
	        }
	        return null;
	    }
	 
	 public SentenceModel getSentenceModel(String language) throws InvalidFormatException, IOException {
	        return initModel(String.format("%s-sent.bin", language),language,SentenceModel.class,null);
	    }
	 
	 public TokenNameFinder getNameFinder(String type, String language) throws IOException {
	        TokenNameFinderModel model = getNameModel(type, language);
	        if(model != null){
	            return new NameFinderME(model);
	        } else {
	            log.info("TokenNameFinder model for type {} and langauge {} not present",type,language);
	            return null;
	        }
	    }
	 
	 public TokenNameFinderModel getNameModel(String type, String language) throws InvalidFormatException, IOException {
	        return initModel(String.format("%s-ner-%s.bin", language, type),language,TokenNameFinderModel.class);
	    }
	 
	 private <T> T initModel(String name,String lang, Class<T> modelType) throws InvalidFormatException, IOException {
	        return initModel(name, lang, modelType,null);
	    }
	 
	 public TokenizerModel getTokenizerModel(String language) throws InvalidFormatException, IOException {
	        return initModel(String.format("%s-token.bin", language),language,TokenizerModel.class);
	    }
	 
	 /**
	 * @param language
	 * @return
	 */
	public Tokenizer getTokenizer(String language) {
	        Tokenizer tokenizer = null;
	        if(language != null){
	            try {
	                TokenizerModel model = getTokenizerModel(language);
	                if(model != null){
	                    tokenizer = new TokenizerME(model);
	                }
	            } catch (InvalidFormatException e) {
	                log.warn("Unable to load Tokenizer Model for "+language+": " +
	                		"Will use Simple Tokenizer instead",e);
	            } catch (IOException e) {
	                log.warn("Unable to load Tokenizer Model for "+language+": " +
	                    "Will use Simple Tokenizer instead",e);
	            }
	        }
	        if(tokenizer == null){
	            log.info("Use Simple Tokenizer for language {}",language);
	            tokenizer = SimpleTokenizer.INSTANCE;
	        } else {
	            log.debug("Use ME Tokenizer for language {}",language);
	        }
	        return tokenizer;
	    }
	 
	 public <T> T getModel(Class<T> modelType,String modelName, Map<String,String> properties) throws InvalidFormatException, IOException {
	        return initModel(modelName, null, modelType, properties);
	    }
	 
	 /**
	 * @param name
	 * @param modelType
	 * @param modelProperties
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	private <T> T initModel(String name,String lang,Class<T> modelType, Map<String,String> modelProperties) throws InvalidFormatException, IOException {
		 
		 InputStream modelDataStream =null;
		 T built = null;
		 try{
			 modelDataStream =  lookupModelStream(name,lang);
			 Constructor<T> constructor;
			 constructor = modelType.getConstructor(InputStream.class);
			 built = constructor.newInstance(modelDataStream);
		 }catch(Exception e){
			 log.error(e.getMessage());
		 }finally{
			 IOUtils.closeQuietly(modelDataStream);
		 }
		 return built;
	 }
	 
	 /**
	 * @param modelName
	 * @return
	 * @throws IOException
	 */
	protected InputStream lookupModelStream(final String modelName,String lang) throws IOException {
		 InputStream modelIn =null;
		/* String workingDir = System.getProperty("user.dir");
		 String modelPath = workingDir+"\\crx-quickstart\\conf\\"+modelName;
		 log.info("Model modelPath : "+modelPath);
		 modelIn = new FileInputStream(modelPath);*/
		 String bundleSymbolicName = "com.sumanta.file.opennlp.ner."+lang;
		 log.info("bundleSymbolicName is {}",bundleSymbolicName);
		 modelIn = bdfp.getInputStream(bundleSymbolicName, modelName, lang,null);
		// modelIn =  this.getClass().getClassLoader().getResourceAsStream(modelName);
		 
		 return modelIn;
	    }
	 
	 
}
