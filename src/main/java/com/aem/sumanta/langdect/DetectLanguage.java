package com.aem.sumanta.langdect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.aem.sumanta.Utils.Utility;

/**
 * @author Sumanta Pakira
 *
 */
public class DetectLanguage {
	
	 private static final Logger log = LoggerFactory.getLogger(DetectLanguage.class);
	 
	 public DetectLanguage() throws LangDetectException {
	        DetectorFactory.clear();
	        try {
	        	 
		   	   DetectorFactory.loadProfile(loadProfiles());
	        	
	   		 } catch (Exception e) {
	            throw new LangDetectException(null, "Error in Initialization: "+e.getMessage());
	        } 
	    }
	 
	 /**
	 * @return
	 * @throws Exception
	 */
	public List<String> loadProfiles() throws Exception {
		 log.info(" Profiling loading is started..");
		 
		 List<String> profiles = new ArrayList<String>();
		 
		    String[] languages =  Utility.getLangConfigurationService().getAllowedLanguages();
	        
	        for (String lang: languages) {
	            String profileFile = "profiles"+"/"+lang;
	            InputStream is = Detector.class.getClassLoader().getResourceAsStream(profileFile);
	            try {
	                String profile = IOUtils.toString(is, "UTF-8");
	                if (profile != null && profile.length() > 0) {
	                    profiles.add(profile);
	                }
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        return profiles;
	    }
	 
	 /**
	 * @param text
	 * @return
	 * @throws LangDetectException
	 */
	public List<Language> getLanguages(String text) throws LangDetectException {
		    Detector detector = DetectorFactory.create();
	        detector.append(text);
	       
	        return detector.getProbabilities();
	    }
	 
	 /**
	 * @param text
	 * @return
	 * @throws LangDetectException
	 */
	public String getLanguage(String text) throws LangDetectException {
	        Detector detector = DetectorFactory.create();
	        detector.append(text);
	        return detector.detect();
	    }

}
