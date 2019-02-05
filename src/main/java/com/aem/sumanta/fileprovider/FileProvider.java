package com.aem.sumanta.fileprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface FileProvider {
	
	InputStream getInputStream(
            String bundleSymbolicName,
            String filename,String lang,
            Map<String,String> comments) throws IOException;

}
