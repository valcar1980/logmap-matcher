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
public class RunMediatingOntologiesPipeline {
	public String parentPath;
	public String sourceOntoPath;
	public String targetOntoPath;
	
	public void getParentFolder(String args[]) {
		try {
			parentPath = args[0];
			File f = new File(parentPath);
			if (f.exists() && f.isDirectory()) {
				System.out.println("Parent folder exists at " + parentPath);
			} else {
				System.out.println("Parent folder doesn't exist at " + parentPath);
				return;
			}
		} catch (Exception e) {
			System.out.println("Parent folder not provided in the args");
			e.printStackTrace();
		
		}
	}
	
	public void readConfigJSON() {
		String jsonPath = parentPath + "config.json";
		File jsonConfig = new File(jsonPath);
		try {
		if (jsonConfig.exists()==false) {
			System.out.println("Couldn't find config JSON in parent folder " + jsonPath);
			return;
		} else {
			
			System.out.println("Found config JSON in parent folder " + jsonPath);
			ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode = objectMapper.readTree(jsonConfig);
	        
	        sourceOntoPath = jsonNode.get("sourceOntologyFullPath").asText();
	        targetOntoPath = jsonNode.get("targetOntologyFullPath").asText();
	        
	        if (new File(sourceOntoPath).exists() && new File(targetOntoPath).exists()) {
	        System.out.println("Found files for the ontologies provided as source " + sourceOntoPath 
	        		+ "\n and as target " +targetOntoPath);
	        }
	        else {
	        	System.out.println("Check your configs, either source or target onto are missing!");
	        }
		}
	} catch (Exception e) {
		System.out.println("Check you program arguments!");
		e.printStackTrace();
		System.out.println(jsonPath);
	}
	}
	
	
	public static void main(String[] args) {

		/*
		 * Hard-coded paths for input
		 */
		
		// String parentPath = "/home/valentina/git-repos/test-data/test-argparse/";
		
		RunMediatingOntologiesPipeline moRunner = new RunMediatingOntologiesPipeline();
		moRunner.getParentFolder(args);
		moRunner.readConfigJSON();
		

		// Expected input
		String onto1_iri = "file:" + moRunner.sourceOntoPath;
		String onto2_iri = "file:" + moRunner.targetOntoPath;
		String s2tFilePath = moRunner.parentPath + "store-source-target/source2target";
		String storeOntoPath = moRunner.parentPath + "/store-mediating-ontologies/";
		int max_mediating_ontologies =12; // overrides default parameter 10
		// Conditional input
		String filePath = moRunner.parentPath + "logmap_top12_mediating_ontologies.txt";
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
		LogMap2_Matcher onto_matcher= onto_mapper.createMappings(onto1_iri, onto2_iri, max_mediating_ontologies);
		Set<MappingObjectStr>  onto_mappings = onto_matcher.getLogmap2_Mappings();
		onto_mapper.saveOntologyMappings(true, onto_mappings, s2tFilePath, onto1_iri, onto2_iri);
		/*
		 * Identify suitable mediating ontologies and store their label onto a list
		 */
		FetchMediatingOntologies mo_fetcher = new FetchMediatingOntologies();
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
		
		
		
		