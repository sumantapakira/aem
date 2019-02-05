package com.aem.sumanta.Utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.aem.sumanta.langdect.LanguageConfigurationService;



/**
 * @author Sumanta Pakira
 *
 */
public class Utility {

/**
 * @return
 */
public static LanguageConfigurationService getLangConfigurationService(){
		
		Bundle bndl = FrameworkUtil.getBundle(com.aem.sumanta.langdect.LanguageConfigurationService.class);
        BundleContext bundleContext = bndl.getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(LanguageConfigurationService.class.getName());
        LanguageConfigurationService allowedLang = (LanguageConfigurationService) bundleContext.getService(ref);
      
        return allowedLang;
	}

/**
 * @param text
 * @return
 */
public static String removeNonUtf8CompliantCharacters(final String text) {
    if (null == text) {
        return null;
    }
    StringBuilder sb = null; 
    for (int i = 0; i < text.length(); i++) {
        int ch = text.codePointAt(i);
        if (!((ch == 0x9) ||
                (ch == 0xA) ||
                (ch == 0xD) ||
                ((ch >= 0x20) && (ch <= 0xD7FF)) ||
                ((ch >= 0xE000) && (ch <= 0xFFFD)) ||
                ((ch >= 0x10000) && (ch <= 0x10FFFF)))){
            if(sb == null){
                sb = new StringBuilder(text);
            }
            sb.setCharAt(i, ' ');
        }
    }
    return sb == null ? text : sb.toString();
}
}
