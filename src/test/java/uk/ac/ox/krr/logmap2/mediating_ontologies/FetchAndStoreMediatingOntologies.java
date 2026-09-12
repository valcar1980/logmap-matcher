package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

/**
 * Takes a parent folder with a config JSON file, reads the input ontologies and finds the mediating ontologies in Bioportal.
 * The class is opinionated in structure of the parent folder and location and name of the config file.
 * To run this pipeline please remember the arguments for the JVM 
 * -Xms500M -Xmx25G -DentityExpansionLimit=10000000 --add-opens=java.base/java.lang=ALL-UNNAMED
 * and the arguments for the program <full path to parent folder>.
 * For example, to run from command line for a 16GB RAM machine (with two different jar files for logmap and the test suite), please run
 * java -Xms500M -Xmx25G -DentityExpansionLimit=10000000 --add-opens=java.base/java.lang=ALL-UNNAMED
 *     -cp logmap-matcher-4.0-tests.jar:logmap-matcher-4.0.jar 
 *         uk.ac.ox.krr.logmap2.mediating_ontologies.RunMediatingOntologiesPipeline /home/valentina/git-repos/test-data/test-argparse/
 * For more details, refer to the README in the package.
 * @author valcar1980
 */
public class FetchAndStoreMediatingOntologies {

	
	
	public static void main(String[] args) {

		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		moUtils.getParentFolder(args);
		moUtils.readConfigJSON();
		moUtils.createSubDirectoriesFromParent();
		

		// Expected input
		String onto1_iri = "file:" + moUtils.sourceOntoPath;
		String onto2_iri = "file:" + moUtils.targetOntoPath;
		String s2tFilePath = moUtils.parentPath + "store-source-target/source2target";
		String storeOntoPath = moUtils.localOntoRepoPath;
		
		//Set up output
		String filePath = null;
		if (moUtils.overrideMOnum == true) {
		// Conditional input
			filePath = moUtils.parentPath + "logmap_top" + Integer.toString(moUtils.maxMONum) + "_mediating_ontologies.txt";
		}else {
			//TODO
			System.out.println("Should read deault paramater and provide max number of mediating ontologies here.");
			filePath = moUtils.parentPath + "logmap_top10_mediating_ontologies.txt";
		}
		boolean txtListExists = false;
		File listFile = new File(filePath);
		
		if (listFile.exists() && listFile.isFile()) txtListExists = true;
		
		//Initialisations
		StoreMediatingOntologies moStorer = new StoreMediatingOntologies();
		List<String> moList = null;
		
		
		// If mediating ontologies files exist, then skip and read the ontologies that need downloading from the file
		
		if (txtListExists == false) {
		System.out.println("Starting Mediating Ontologies Pipeline");
		CreateMappingsBetweenTwoOntologies onto_mapper = new CreateMappingsBetweenTwoOntologies();
		LogMap2_Matcher onto_matcher= onto_mapper.createMappings(onto1_iri, onto2_iri, moUtils.maxMONum);
		Set<MappingObjectStr>  onto_mappings = onto_matcher.getLogmap2_Mappings();
		onto_mapper.saveOntologyMappings(onto_mappings, s2tFilePath, onto1_iri, onto2_iri);
		/*
		 * Identify suitable mediating ontologies and store their label onto a list
		 */
		MediatingOntologiesUtils mo_fetcher = new MediatingOntologiesUtils();
		moList = mo_fetcher.extractMediatingOntologyList(onto_matcher);
		mo_fetcher.saveListMediatingOntolgies(true, moList, filePath);
		}
		else {
			System.out.println("Mediating ontologies list already exists at " + filePath + " \n Skipping to fetching ontologies from list");
			try {
				moList = moStorer.getOntologyListFromFile(filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * Store all ontologies from list of mediating ontologies
		 */
		int countOnto = moList.size();
		System.out.println("There are" + countOnto + "mediating ontologies in the list");
		
		
			int all_counter = 0; // all counts, including failed downloads
			int success_counter =0; // existing ontologies or successfully downloaded
			for (String ontoStr : moList) {
				all_counter += 1;
				
				if (success_counter ==10) {
					System.out.println("Stopping downloads, we have reached 10 ontologies");
					break;
				}
				System.out.println("Fetching ontology No.  " + all_counter + " from list,  label:  " + ontoStr);


				boolean isOntoThere = moStorer.checkOntoPath(ontoStr, storeOntoPath, ".owl");

				if (isOntoThere == true) {

					System.out.println("Ontology file already exists at location, skipping");
					success_counter++;
					continue;
				}
				else {
					try {
					OWLOntology moDownload = moStorer.CallBioPortal(ontoStr, storeOntoPath);
					System.out.println("Fetched ontology " + ontoStr);
	
					
					moStorer.saveOntology(ontoStr, moDownload, storeOntoPath);
					System.out.println("Stored ontology " + ontoStr);
					success_counter++;
					}
					catch (Exception e) 
					{
						System.out.println("Coudln't fetch " + ontoStr + "; skipping");
						continue;
						//e.printStackTrace();
					}

				}
					
				// we update the counter regardless of successful download
			}
		
		
		System.out.println("All Ontologies are stored in " + storeOntoPath);
		}
	}
		
		
		
		