package com.aem.sumanta.dbpedia.ontology;

public enum NamespaceEnum {

	dbpedia_ont("dbpedia-ont", "http://dbpedia.org/ontology/"),
    rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    rdfs("http://www.w3.org/2000/01/rdf-schema#"),
    dc("http://purl.org/dc/terms/"),
    skos("http://www.w3.org/2004/02/skos/core#");
	
	 String ns;
	    String prefix;

	    NamespaceEnum(String ns) {
	        if (ns == null) {
	            throw new IllegalArgumentException("The namespace MUST NOT be NULL");
	        }
	        this.ns = ns;
	    }

	    NamespaceEnum(String prefix, String ns) {
	        this(ns);
	        this.prefix = prefix;
	    }

	    public String getNamespace() {
	        return ns;
	    }

	    public String getPrefix() {
	        return prefix == null ? name() : prefix;
	    }
}
