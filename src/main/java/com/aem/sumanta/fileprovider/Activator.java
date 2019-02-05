package com.aem.sumanta.fileprovider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator
  implements BundleActivator
{
  private FileBundleInstaller installer;
  private static final Logger log = LoggerFactory.getLogger(Activator.class);
  
  public void start(BundleContext context)
    throws Exception
  {
	 this.installer = new FileBundleInstaller(context);
  }
  
  public void stop(BundleContext context)
    throws Exception
  {
	this.installer.close();
    this.installer = null;
  }
}
