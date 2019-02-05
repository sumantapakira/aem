package com.aem.sumanta.fileprovider;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBundleInstaller implements BundleListener{
	
	private static final Logger log = LoggerFactory.getLogger(FileBundleInstaller.class);
	private final Map<Bundle, ServiceRegistration> activated = new HashMap();
	private  BundleContext context = null;
	
	 public FileBundleInstaller(BundleContext context)
	  {
		 if (context == null) {
	      throw new IllegalArgumentException("The BundleContext MUST NOT be NULL");
	    }
	    this.context = context;
	    this.context.addBundleListener(this);
	    
	    registerActive(this.context);
	  }
	 
	 private void registerActive(BundleContext context)
	  {
	    for (Bundle bundle : context.getBundles()) {
	      if ((bundle.getState() & 0x28) != 0) {
	        register(bundle);
	      }
	    }
	  }

	@Override
	public void bundleChanged(BundleEvent event) {
		
		switch (event.getType())
	    {
	    case 2: 
	      register(event.getBundle());
	      break;
	    case 4: 
	      unregister(event.getBundle());
	      break;
	    case 8: 
	      unregister(event.getBundle());
	      register(event.getBundle());
	    }
		
	}
	
	private void register(Bundle bundle)
	  {
	    synchronized (this.activated)
	    {
	      if (this.activated.containsKey(bundle)) {
	        return;
	      }
	      this.activated.put(bundle, null);
	    }
	    log.debug("Register Bundle {} with FileBundleInstaller", bundle.getSymbolicName());
	    Dictionary<String, String> headers = bundle.getHeaders();
	    
	    String pathsString = (String)headers.get("Data-Files");
	    if (pathsString != null)
	    {
	      Dictionary<String, Object> properties = new Hashtable();
	      String dataFilesRankingString = (String)headers.get("Data-Files-Priority");
	      if (dataFilesRankingString != null) {
	        try
	        {
	          properties.put("service.ranking", Integer.valueOf(dataFilesRankingString));
	        }
	        catch (NumberFormatException e)
	        {
	          log.warn("Unable to parse integer value for '{}' from the configured value '{}'. Will use default ranking", "Data-Files-Priority", dataFilesRankingString);
	        }
	      }
	      List<String> paths = Arrays.asList(pathsString.replaceAll("\\s", "").split(","));
	      
	      BundleDataFileProvider provider = new BundleDataFileProvider(bundle, paths);
	      properties.put("service.description", String.format("%s for Bundle %s and Paths %s", new Object[] { BundleDataFileProvider.class.getSimpleName(), bundle.getSymbolicName(), provider.getSearchPaths() }));
	      
	      ServiceRegistration registration = this.context.registerService(FileProvider.class.getName(), provider, properties);
	      
	      log.info("Registerd BundleResourceProvider for {} and relative paths {}", this.context.getBundle().getSymbolicName(), provider.getSearchPaths());
	      synchronized (this.activated)
	      {
	        if (this.activated.containsKey(bundle)) {
	          this.activated.put(bundle, registration);
	        } else {
	          registration.unregister();
	        }
	      }
	    }
	  }
	
	private void unregister(Bundle bundle)
	  {
	    synchronized (this.activated)
	    {
	      if (!this.activated.containsKey(bundle)) {
	        return;
	      }
	      ServiceRegistration registration = (ServiceRegistration)this.activated.remove(bundle);
	      if (registration != null)
	      {
	        log.info("Unregister BundleDataFileProvider for Bundel {}", bundle.getSymbolicName());
	        registration.unregister();
	      }
	    }
	  }
	
	public void close()
	  {
		 this.context.removeBundleListener(this);
	    synchronized (this.activated)
	    {
	      for (Map.Entry<Bundle, ServiceRegistration> entry : this.activated.entrySet()) {
	        if (entry.getValue() != null)
	        {
	          log.info("Unregister BundleDataFileProvider for Bundel {}", ((Bundle)entry.getKey()).getSymbolicName());
	          ((ServiceRegistration)entry.getValue()).unregister();
	        }
	      }
	    }
	  }

}
