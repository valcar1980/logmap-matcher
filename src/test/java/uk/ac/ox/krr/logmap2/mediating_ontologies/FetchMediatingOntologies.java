package uk.ac.ox.krr.logmap2.mediating_ontologies;

public class FetchMediatingOntologies {
	
	public FetchMediatingOntologies(){}
	
	public static void main(String[] args) {
		System.out.println("Fetching");
		
		  FindAndStoreTopMediatingOntologies myLogMap = new
		  FindAndStoreTopMediatingOntologies();
		  
		  LogMap2_Matcher onto_mappings = myLogMap.createMappings(onto1_iri,onto2_iri);
		  printOntologyMappings(onto_mappings); Set<String> representative_labels =
		  onto_mappings.getRepresentativeLabelsForMappings(); long endTime =
		  System.nanoTime(); System.out.println("Map matching task completed.\t" +
		  Math.floor((endTime-startTime)/10e9) + " seconds elapsed");
		  
		  startTime = System.nanoTime(); MediatingOntologyExtractor mo_extract = new
		  MediatingOntologyExtractor(representative_labels); endTime =
		  System.nanoTime(); System.out.println("Extracted mediating ontologies.\t" +
		  Math.floor((endTime-startTime)/10e9) + " seconds elapsed"); List<String>
		  mediating_ontologies = mo_extract.getSelectedMediatingOntologies();
		  printMediatingOntologies(mediating_ontologies);
		 
	}
	
	
	
}