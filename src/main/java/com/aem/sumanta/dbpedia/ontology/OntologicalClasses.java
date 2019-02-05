package com.aem.sumanta.dbpedia.ontology;

public class OntologicalClasses {
	
	public static final String DBPEDIA_PERSON = new String(
            NamespaceEnum.dbpedia_ont+"Person");

    public static final String DBPEDIA_PLACE = new String(
            NamespaceEnum.dbpedia_ont+"Place");

    public static final String DBPEDIA_ORGANISATION = new String(
            NamespaceEnum.dbpedia_ont+"Organisation");

    public static final String SKOS_CONCEPT = new String(
        NamespaceEnum.skos+"Concept");
    
    public static final String DC_LINGUISTIC_SYSTEM = new String(
        NamespaceEnum.dc+"LinguisticSystem");

    private OntologicalClasses() {
    }

}
