package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.util.List;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;


public class RunMediatingOntologiesPipeline {

	public static void main(String[] args) {

		/*
		 * Hard-coded paths for input
		 */

		String onto1_iri = "file:/home/valentina/Downloads/anatomy-dataset/human.owl";
		String onto2_iri = "file:/home/valentina/Downloads/anatomy-dataset/mouse.owl";

		String parentPath = "/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/";
		String filePath = parentPath + "logmap_top10_mediating_ontologies.txt";

		System.out.println("Starting Mediating Ontologies Pipeline");
		CreateMappingsBetweenTwoOntologies onto_mapper = new CreateMappingsBetweenTwoOntologies();
		LogMap2_Matcher onto_mappings = onto_mapper.createMappings(onto1_iri, onto2_iri);

		/*
		 * Identify suitable mediating ontologies and store their label onto a list
		 */
		FetchMediatingOntologies mo_fetcher = new FetchMediatingOntologies();
		List<String> moList = mo_fetcher.extractMediatingOntologyList(onto_mappings);
		mo_fetcher.saveListMediatingOntolgies(true, moList, filePath);

		/*
		 * Store all ontologies from list of mediating ontologies
		 */
		StoreMediatingOntologies moStorer = new StoreMediatingOntologies();
		int countOnto = moList.size();
		System.out.println("There are" + countOnto + "mediating ontologies in the list");
		
		try {
			int counter = 1;
			for (String ontoStr : moList) {

				System.out.println("Fetching ontology No.  " + counter + " label:  " + ontoStr);

				// TODO if file exists, skip

				boolean isOntoThere = moStorer.checkOntoPath(ontoStr, parentPath);

				if (isOntoThere == true) {

					System.out.println("Ontology file already exists at location, skipping");
					continue;
				}
				OWLOntology moDownload = moStorer.CallBioPortal(ontoStr, parentPath);
				System.out.println("Fetched ontology" + ontoStr);

				// TODO store the ontology
				moStorer.saveOntology(ontoStr, moDownload, parentPath);
				System.out.println("Stored ontology" + ontoStr);

				counter += 1;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		System.out.println("All Stored");
}
}
		
		
		
		