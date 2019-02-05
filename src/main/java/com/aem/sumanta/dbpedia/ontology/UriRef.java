package com.aem.sumanta.dbpedia.ontology;



public class UriRef {
	
	private String unicodeString;
	String personOntology = "http://dbpedia.org/ontology/Person";
	String placeOntology = "http://dbpedia.org/ontology/Place";
	String OrgOntology = "http://dbpedia.org/ontology/Organisation";
	
	public UriRef(String unicodeString) {
		if(unicodeString.equals("person")){
			this.unicodeString = personOntology;
		}else if(unicodeString.equals("location")){
			this.unicodeString = placeOntology;
		}else if(unicodeString.equals("organization")){
			this.unicodeString = OrgOntology;
		}
		
	}
  
	 public String getUnicodeString() {
		       return unicodeString;
		   }
	 
	 @Override
	  public boolean equals(Object obj) {
	 
	   if (!(obj instanceof UriRef)) {
		   return false;
	       }
	 
	     return unicodeString.equals(((UriRef) obj).getUnicodeString());
	   }
	 
	 @Override
	    public int hashCode() {
	        int hash = 5 + unicodeString.hashCode();
	        return hash;
	    }
	 
}
