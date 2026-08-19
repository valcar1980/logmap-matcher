package uk.ac.ox.krr.logmap2.mediating_ontologies;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;

public class RunMediatingOntologiesPipeline {
	
	public static void main(String[] args) {
		
		
		
		String onto1_iri = "file:/home/valentina/Downloads/anatomy-dataset/human.owl"; 
		String onto2_iri = "file:/home/valentina/Downloads/anatomy-dataset/mouse.owl";
		
		System.out.println("Starting Mediating Ontologies Pipeline");
		CreateMappingsBetweenTwoOntologies onto_mapper = new CreateMappingsBetweenTwoOntologies();
		LogMap2_Matcher onto_mappings = onto_mapper.createMappings(onto1_iri, onto2_iri);
		
		FetchMediatingOntologies fetch_mo = new FetchMediatingOntologies();
		fetch_mo.main(args);
		
		StoreMediatingOntologies store_mo = new StoreMediatingOntologies();
		store_mo.main(args);
	}
	
	
	
}