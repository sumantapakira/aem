package com.aem.sumanta.langdect.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Activate;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.sumanta.langdect.LanguageConfigurationService;



@Service
@Component(immediate=true, metatype = true, label = "Supported Language list", description = "This is a Apache tika LanguageIdentifier properties file")
public class LanguageConfigurationServiceImpl implements LanguageConfigurationService{
	
	private static final Logger log = LoggerFactory.getLogger(LanguageConfigurationServiceImpl.class);
	
	private static final String DEFAULT_ALLOWED_LANGUAGE = "en";
	@Property(unbounded = PropertyUnbounded.ARRAY, description = "List of suppoerted languages.", 
			value = DEFAULT_ALLOWED_LANGUAGE, label = "List of suppoerted languages.")
	private static final String ALLOWED_LANGUAGES = "config.allowed.languages";
	private String[] defaultAllowedLanguages;
	
	@Activate
	protected void activate(ComponentContext ctx) throws ConfigurationException{
		defaultAllowedLanguages = PropertiesUtil.toStringArray(ctx.getProperties()
				.get(ALLOWED_LANGUAGES));
	}
	

	@Override
	public String[] getAllowedLanguages() {
				
		return defaultAllowedLanguages ;
		}
	
	
	

}
