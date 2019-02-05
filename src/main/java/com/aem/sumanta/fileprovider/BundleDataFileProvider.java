package com.aem.sumanta.fileprovider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(immediate=true, metatype = false, label = "NER File provider", description = "Loads all the NER file.")
public class BundleDataFileProvider implements FileProvider{
	
	private static final Logger log = LoggerFactory.getLogger(BundleDataFileProvider.class);
	private Bundle bundle;
	private List<String> searchPaths;
	
	public BundleDataFileProvider(){
		super();
	}
	
	public BundleDataFileProvider(Bundle bundle, List<String> searchPaths)
	  {
	    if (bundle == null) {
	      throw new IllegalArgumentException("The parsed BundleContext MUST NOT be NULL!");
	    }
	    this.bundle = bundle;
	    if ((searchPaths == null) || (searchPaths.isEmpty()))
	    {
	      this.searchPaths = Collections.singletonList(File.separator);
	    }
	    else
	    {
	      List<String> paths = new ArrayList(searchPaths.size());
	      for (String path : searchPaths)
	      {
	        if (path == null)
	        {
	          path = "/";
	        }
	        else
	        {
	          path = FilenameUtils.separatorsToUnix(path);
	          if (!path.endsWith("/")) {
	            path = path + '/';
	          }
	        }
	        if (!paths.contains(path)) {
	          paths.add(path);
	        }
	      }
	      this.searchPaths = Collections.unmodifiableList(paths);
	    }
	  }

	@Override
	public InputStream getInputStream(String bundleSymbolicName,
			String filename,String lang, Map<String, String> comments) throws IOException {
		log.info("-----------------getInputStream--------------");
		URL resource = getDataFile(bundleSymbolicName, filename,lang);
	    log.info("Resource {} found: {}", resource == null ? "NOT" : "", filename);
	    return resource != null ? resource.openStream() : null;
	}
	
	private URL getDataFile(String bundleSymbolicName, String filename, String lang)
	  {
		 URL resource = null;
		try{
	    if ((bundleSymbolicName != null) && (!this.bundle.getSymbolicName().equals(bundleSymbolicName)))
	    {
	      this.log.info("Requested bundleSymbolicName {} does not match mine ({}), request ignored", bundleSymbolicName, this.bundle.getSymbolicName());
	      
	      return null;
	    }
	   
	    Iterator<String> relativePathIterator = this.searchPaths.iterator();
	    while ((resource == null) && (relativePathIterator.hasNext()))
	    {
	      String path = (String)relativePathIterator.next();
	      String resourceName = path != null ? path + "ner/"+lang +"/" +filename : filename;
	      resource = this.bundle.getEntry(resourceName);
	      log.debug("Resource is: {}",resource.getPath());
	    }
		}catch(Exception e){
			log.error(e.getMessage());
		}
	    return resource;
	  }

	public final List<String> getSearchPaths()
	  {
	    return this.searchPaths;
	  }
}
