package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


import uk.ac.ox.krr.logmap2.GetRepresentativeLabelsSetForMappings;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.OntologyLoader;
import uk.ac.ox.krr.logmap2.bioportal.MediatingOntologyExtractor;
import uk.ac.ox.krr.logmap2.bioportal.RESTBioPortalAccess;
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
	
	
public List<String> getOntologyListFromFile(String filePath) throws IOException {
		
		// list that holds strings of a file
        List<String>  listOfStrings = new ArrayList<String>();
      
        // load data from file
        BufferedReader bf = new BufferedReader(new FileReader(filePath));
      
        // read entire line as string
        String line = bf.readLine();
      
        // checking for end of file
        while (line != null) {
        	if(line.startsWith("#")) {
        		System.out.println("Skipping MO " + line + "from list of ontologies.");
                line = bf.readLine();

        	}
        	else {
            listOfStrings.add(line);
            line = bf.readLine();

        	}
        }
      
        // closing bufferreader object
        bf.close();
      		
		return listOfStrings;
	}	
			
	public void saveOntology(String ontoLabel, OWLOntology inputOntology, String basePath){
	
	
			//Create a file for the new format
			String mo_output_path = basePath+ ontoLabel + ".owl";
			System.out.println("Storing ontology at " + mo_output_path);
			File outFile= new File(mo_output_path);
			OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
			OWLDocumentFormat format = new RDFXMLDocumentFormat();
			IRI outputStream = IRI.create(outFile);
			try {
				onto_manager.saveOntology(inputOntology, format, outputStream);
			} catch (OWLOntologyStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
	public OWLOntology CallBioPortal(String onto_label, String base_output_path) {
		
		RESTBioPortalAccess bioportal = new RESTBioPortalAccess();
		if (bioportal.isActive()) {
		
			System.out.println("BioPortal is active: ");
			
			OWLOntology moDownload = bioportal.downLoadOntology(onto_label, 3);
			System.out.println("Downloaded ontology" + moDownload.getOntologyID());
			
			return moDownload;
		}
		else {
			// TODO return an empty ontology or throw exception
			return null;
		}
	}
	
	public void saveOntologiesFromList() {
		//TODO clean up the main
		
	}
	
	public boolean checkOntoPath(String ontoStr, String basePath, String fileExtension) {
		// TODO Auto-generated method stub
		String ontoPath = basePath + ontoStr + fileExtension;
		File ontoFile = new File(ontoPath);
		
		if (ontoFile.exists() && ontoFile.isFile()) {
			//System.out.println(ontoFile.getName());
			return true;
		}else {
		return false;
		}
	}
		
	
	
	
	
	
	protected static File findFlatFormatInDir(String dirPath) {
		File dir = new File(dirPath);
		File firstFile = null;
		if(dir.exists()) {
			
			
			
			   // Create a FilenameFilter
            FilenameFilter filter = new FilenameFilter() {

                public boolean accept(File f, String name)
                {
                    return (name.endsWith(".txt") || name.endsWith(".rdf"));
                }
            };
            
            File[] listFiles = dir.listFiles(filter);
            // Display the names of the files
            for (int i = 0; i < listFiles.length; i++) {
                System.out.println(listFiles[i].getName());			
                }
            if (listFiles.length>0) {
			System.out.println("Assuming first txt/rdf file in folder is what you want");
			firstFile = listFiles[0];	
			System.out.println(firstFile);
			}
			}
			return firstFile;
		}
		

	
	public static void main(String[] args) {

		/*
		 * Input data
		 */
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		moUtils.getParentFolder(args);
		moUtils.readConfigJSON();
		moUtils.createSubDirectoriesFromParent();
		System.out.println("\n\n Debug parentPath \t\t" + moUtils.parentPath);
		
		
		String onto1_iri = "file:" + moUtils.sourceOntoPath;
		String onto2_iri = "file:" + moUtils.targetOntoPath;
		String s2tFilePath = moUtils.sourceToTargetDirPath + "source2target";
		String storeOntoPath = moUtils.localOntoRepoPath;
		
		
		//Other Initialisations
		FetchAndStoreMediatingOntologies moStorer = new FetchAndStoreMediatingOntologies();
		List<String> moList = null;
		CreateMappingsBetweenTwoOntologies onto_mapper = new CreateMappingsBetweenTwoOntologies();
		Set<MappingObjectStr>  s2tOnto_mappings = Collections.emptySet();
		
		// Conditional input - list of mediating ontologies
		String listMOFilePath = null;
		if (moUtils.overrideMOnum == true) {
			listMOFilePath = moUtils.parentPath + "logmap_top" + Integer.toString(moUtils.maxMONum) + "_mediating_ontologies.txt";
		}else {
			//TODO
			System.out.println("Should read deault paramater and provide max number of mediating ontologies here.");
			listMOFilePath = moUtils.parentPath + "logmap_top10_mediating_ontologies.txt";
		}
		
		// If we have a file with the list of mediating ontologies, we can start reading from it already
		File listFile = new File(listMOFilePath);

		
		if (listFile.exists() && listFile.isFile()) {
			System.out.println("Mediating ontologies list already exists at " 
		+ listMOFilePath + " \n Skipping to fetching ontologies from list");
			try {
				moList = moStorer.getOntologyListFromFile(listMOFilePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
		System.out.println("Mediating ontology list is missing, proceeding to create one.");


		// If ontology maps already exist, skip and move to checking whether the list of MO exists
		System.out.println("First, we check if we have mappings between source and target.");
		//TODO can we check without loading the whole file?
		System.out.println("\n\n Debug \t\t" + moUtils.sourceToTargetDirPath);
		
		
		// TODO check if there is any file that could be used as such, otherwise invoke CreateComposedMappingsBetweenTwoOntologies!
		File thereMayBeFile = findFlatFormatInDir(moUtils.sourceToTargetDirPath);
		Set<MappingObjectStr> source2targetMappings;
		if(thereMayBeFile!=null) {
			source2targetMappings = moUtils.readMappingsFromFile(moUtils.sourceToTargetDirPath + thereMayBeFile.toString()); 
		} else {
			source2targetMappings = Collections.emptySet();	
		}
		if (source2targetMappings.size()>0) {
			System.out.println("Found mappings between source and target, no need to create them.");
			
			// We need the representative labels to find the mediating ontologies
			Set<String> s2tRepLabels = Collections.emptySet();
			try {
			OntologyLoader loader1 = new OntologyLoader(onto1_iri);
			OntologyLoader loader2 = new OntologyLoader(onto2_iri);
			
			GetRepresentativeLabelsSetForMappings representativeLabelExtractor = 
					new GetRepresentativeLabelsSetForMappings(
							loader1.getOWLOntology(), 
							loader2.getOWLOntology(), 
							source2targetMappings);
			
			s2tRepLabels = representativeLabelExtractor.getRepresentativeLabels();
			MediatingOntologyExtractor mo_extract = new MediatingOntologyExtractor(s2tRepLabels);
			moList = mo_extract.getSelectedMediatingOntologies();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else
		{ 
			System.out.println("No mappings found between source and target, running Logmap now");
			LogMap2_Matcher onto_matcher= onto_mapper.createMappings(onto1_iri, onto2_iri, moUtils.maxMONum);
			s2tOnto_mappings = onto_matcher.getLogmap2_Mappings();
			moUtils.saveOntologyMappings(s2tOnto_mappings, s2tFilePath, onto1_iri, onto2_iri);
			/*
			 * Identify suitable mediating ontologies and store their label onto a list
			 */
			moList = moUtils.extractMediatingOntologyList(onto_matcher);
		}
		
		// save moList to file
		moUtils.saveListMediatingOntolgies(moList, listMOFilePath);
		
	}	


		/*
		 * Store all ontologies from list of mediating ontologies
		 */
		int countOnto = moList.size();
		System.out.println("There are " + countOnto + " mediating ontologies in the list");
		System.out.println("Starting Mediating Ontologies fetching step");

		
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
		
		
		
		