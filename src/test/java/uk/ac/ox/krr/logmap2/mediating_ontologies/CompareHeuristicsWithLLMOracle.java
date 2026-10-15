/*
 * Run the heuristics previously devised as part of LogMapBio to select "reliable" mappings from ontology
 *  alignment and compare with the LLM-oracle that has the same model and setup as LogMap-LLM
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.LogMap2_OAEI_BioPortal;
import uk.ac.ox.krr.logmap2.Parameters;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;


public class CompareHeuristicsWithLLMOracle{
	
	
	protected static void writeVotingMatrixToFile(HashMap<MappingObjectStr, MappingMetadata> votingMatrix, String outPath) {
		File votingMatrixFile = new File(outPath);
		BufferedWriter bf = null;
		
		try {

            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(votingMatrixFile));
		
			for(MappingObjectStr key:votingMatrix.keySet()) {
				//System.out.println(key);


	                // put key and value separated by a colon
	                bf.write(key + "\t"
	                         + votingMatrix.get(key).toString());

	                // new line
	                bf.newLine();
	            }

	            bf.flush();
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	
		try {
			bf.close();
		}catch(Exception e) {}
		
		
		
	}
	
	public static void main (String[] args) {
		
		LogMap2_OAEI_BioPortal logmap_Bio = new LogMap2_OAEI_BioPortal();
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		
		// Take onto1 and onto2
		String onto1Path = "file:/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/mouse.owl";
		String onto2Path = "file:/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/human.owl";
		String iri1 = onto1Path;
		String iri2 = onto2Path;
		
		// New Mappings are our composed mappings - logmapLLM mappings
		String mappingPath = "/home/valentina/Data/OAEI-output/oaei-2025-anatomy/oaei-human-mouse/composed-mappings-minus-llm-default/all-composed-minus-llm-default.txt";
		Set<MappingObjectStr> newMappings = moUtils.readMappingsFromFile(mappingPath);
		
		
		System.out.println("There are " + newMappings.size() + " new mappings (i.e. composed minus logmap-llm) as baseline");
		ProcessComposedMappings cheekySwapper = new ProcessComposedMappings();
		Boolean sameMappingOrder;
		
		/*
		 * Create the voting matrix, key: mapping, value: [] empty array, which will be filled later
		 */
		
		
		 double confidenceLowThreshold = Parameters.confidence_composed_mappings2; 
		 double confidenceHighThreshold = Parameters.confidence_composed_mappings1;
		 System.out.println("Heuristics based on 2+ votes and confidence >= " + confidenceLowThreshold 
		 + "or 1 vote and confidence >= " + confidenceHighThreshold);
		
		HashMap<MappingObjectStr, MappingMetadata> votingMatrix = new HashMap<MappingObjectStr, MappingMetadata>();
		for(MappingObjectStr mapping: newMappings) {
			
			MappingMetadata meta = new MappingMetadata();
			meta.setConfidence(mapping.getConfidence());
			//Assign confidence and initialise the rest
			votingMatrix.put(mapping, meta);
		}
		
		System.out.println("Sanity check, there are " + votingMatrix.keySet().size() + " mappings in the voting matrix");
		
		/*
		 * Retrieve source of mapping from all mediating ontologies
		 */
		
		String moMappingFolderPath = "/home/valentina/Data/tests/oaei-mouse-human/store-composed-mappings/";
		File moMappingFolder = new File(moMappingFolderPath);
		
		
		// We filter txt only, because the folder contains also a tsv for each MO
		//TODO use hashmap key uniqueness instead
		FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File f, String name)
            {
                return (name.endsWith(".txt"));
            }
        };
		
		if(moMappingFolder.isDirectory()) {
			File[] fileList = moMappingFolder.listFiles(filter);
			for(File f: fileList) {
				//open file and extract mappings
				String ontoLabel = f.getName().substring(0, f.getName().lastIndexOf('.'));
				System.out.println("Extracting votes for mediating ontology " + ontoLabel);
				Set<MappingObjectStr>  moMappings = moUtils.readMappingsFromFile(f.getAbsolutePath());
				if(moMappings.isEmpty()==true) continue; //skipping empty sets
				//loop through mappings
				sameMappingOrder= cheekySwapper.compareMappingOrder(moMappings, newMappings);
				if (sameMappingOrder==false) {
					moMappings = cheekySwapper.swapMappingOrder(moMappings);
					System.out.println("Had to reverse mapping directions for ontology " + ontoLabel);
				}
				for(MappingObjectStr map:moMappings) {
					if(newMappings.contains(map)) {
						//System.out.println("Match in " + ontoLabel + " for mapping " + map);
						//ArrayList<String> labels = votingMatrix.get(map).get("moList");
				        //if (labels != null) {
				         //   labels.add(ontoLabel);
						MappingMetadata currentMeta = votingMatrix.get(map);
						MappingMetadata updateMeta = new MappingMetadata(currentMeta);
						updateMeta.addToVotingCount();
						updateMeta.addToMOList(ontoLabel);
						votingMatrix.put(map, updateMeta);
					}
				}
			}
		}
			
		
		String outPath = "/home/valentina/Data/OAEI-output/oaei-2025-anatomy/oaei-human-mouse/testVoting/votingMatrix.txt";
		writeVotingMatrixToFile(votingMatrix, outPath);
		

		
		/*
		 * Apply heuristics to mappings
		 */
		

		
		 
		
		/*
		 * LLM-oracle annotated mappings
		 */
		
		
		//Upload llm-annotation
		
//		String oldMappingPath = "/home/valentina/Data/tests/oaei-mouse-human/logmapbioheu/logmapbiomappings.txt";
//		Set<MappingObjectStr> oldMappings = moUtils.readMappingsFromFile(oldMappingPath);
//		
//		
//
//		String fileOut = "/home/valentina/Data/tests/oaei-mouse-human/logmapbioheu/logmapbiomappings";

//		//Run LogMapBio from scratch
//		try {
//			logmap_Bio.align(iri1, iri2); // this gets also the 10 mediating ontologies :O
//			//logmap_Bio.returnAlignmentFile(file_out);
//			
//			Set<MappingObjectStr> logmapBioMappings = logmap_Bio.getLogMapBio_Mappings();
//			moUtils.saveOntologyMappings(logmapBioMappings, fileOut,iri1, iri2 ); 			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// TODO:Run matcher using same new mappings as different experiment
//		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
//		// In case an import is broken
//		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
//		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
//		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
//		onto_manager.setOntologyLoaderConfiguration(config);
//		try {
//		OWLOntology onto1 = onto_manager.loadOntology(IRI.create(iri1));
//		OWLOntology onto2 = onto_manager.loadOntology(IRI.create(iri2));
//		
//		LogMap2_Matcher logmapBioMappingsV2 = new LogMap2_Matcher(onto1,onto2, newComposedMappings);
//		
//		String fileOutV2 = fileOut + "V2";
//		moUtils.saveOntologyMappings(logmapBioMappingsV2.getLogmap2_Mappings(), fileOutV2, iri1, iri2);
//	}catch(Exception e){
//		e.printStackTrace();
//	}
//	}
	
}}

	
	
	
	