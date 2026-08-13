package uk.ac.ox.krr.logmap2.mediating_ontologies;


public class RunMediatingOntologiesPipeline {
	
	public static void main(String[] args) {
		System.out.println("Starting Mediating Ontologies Pipeline");
		FetchMediatingOntologies fetch_mo = new FetchMediatingOntologies();
		fetch_mo.main(args);
		
		SaveMediatingOntologiesList save_mo_list = new SaveMediatingOntologiesList();
		save_mo_list.main(args);
		
		StoreMediatingOntologies store_mo = new StoreMediatingOntologies();
		store_mo.main(args);
	}
	
	
	
}