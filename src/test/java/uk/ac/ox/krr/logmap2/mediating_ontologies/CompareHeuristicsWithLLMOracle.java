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
import java.util.HashSet;
import java.util.Hashtable;
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
	final double confidenceLowThreshold = Parameters.confidence_composed_mappings2; 
	final double confidenceHighThreshold = Parameters.confidence_composed_mappings1;
	
	
	public Boolean applyNaiveHeuristicFilter(Double confidence, Integer voteCount) {
		Boolean passThreshold = false;
		
		if((voteCount>=2 && confidence > confidenceLowThreshold) ||
		(voteCount == 1 && confidence > confidenceHighThreshold)){
			passThreshold = true;
		}
		
		
		return passThreshold;
	}
	
	
	
	
	/**
	 * Create a tabular file with all the mappings and how many mediating ontologies voted for each of them
	 * @param votingMatrix 
	 * @param outPath
	 */
	public void writeVotingMatrixToFile(HashMap<MappingObjectStr, MappingMetadata> votingMatrix,
			String outPath, Boolean displayPassThreshold) {
		File votingMatrixFile = new File(outPath);
		BufferedWriter bf = null;

		try {

			// create new BufferedWriter for the output file
			bf = new BufferedWriter(new FileWriter(votingMatrixFile));
			if(displayPassThreshold==false) {
				bf.write("#\t Mapping \t Confidence \t VoteCount \t MediatingOntologies");}
			else {
				bf.write("#\t Mapping \t Confidence \t VoteCount \t passThreshold \t MediatingOntologies");

			}
			bf.newLine();

			for (MappingObjectStr key : votingMatrix.keySet()) {
				// System.out.println(key);

				// put key and value separated by a colon
				bf.write(key + "\t" + votingMatrix.get(key).toString(displayPassThreshold));

				// new line
				bf.newLine();
			}

			bf.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			bf.close();
		} catch (Exception e) {
		}

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
		
		CompareHeuristicsWithLLMOracle comparer = new CompareHeuristicsWithLLMOracle();
		
		HashMap<MappingObjectStr, MappingMetadata> votingMatrix = new HashMap<MappingObjectStr, MappingMetadata>();
		for(MappingObjectStr mapping: newMappings) {
			
			MappingMetadata meta = new MappingMetadata();
			meta.setConfidence(mapping.getConfidence());
			//Assign confidence and initialise the rest
			votingMatrix.put(mapping, meta);
		}
		
		//System.out.println("Sanity check, there are " + votingMatrix.keySet().size() + " mappings in the voting matrix");
		
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
		comparer.writeVotingMatrixToFile(votingMatrix, outPath, false);
		

		
		/*
		 * Apply heuristics to mappings
		 */
		
		// At this point I could even load a votingMatrix file and apply any heuristic I want
		 System.out.println("Heuristics based on 2+ votes and confidence >= " + comparer.confidenceLowThreshold 
		 + "or 1 vote and confidence >= " + comparer.confidenceHighThreshold);
		 Set<MappingObjectStr> heuristicsFilteredMappings = new HashSet<MappingObjectStr>();
		 Integer countPassedThreshold = 0;
		for(HashMap.Entry<MappingObjectStr,MappingMetadata> entry:votingMatrix.entrySet()) {
	        {
	            MappingObjectStr mapping = entry.getKey();
	            MappingMetadata currentTabular = entry.getValue();
	            MappingMetadata updateTabular = new MappingMetadata(currentTabular);
	            
	            //use key and value
	            Boolean passThreshold = comparer.applyNaiveHeuristicFilter(currentTabular.getConfidence(), currentTabular.getVotingCount());
	            updateTabular.setPassThreshold(passThreshold);
	            votingMatrix.put(mapping, updateTabular);
	            
	            if(passThreshold == true) {
	            	countPassedThreshold++;
	            	heuristicsFilteredMappings.add(mapping);
	            }
	        }
		}
		System.out.println("The mappings passing the heuristic threshold are " + countPassedThreshold + " out of " + votingMatrix.size());
		String outPath2 = "/home/valentina/Data/OAEI-output/oaei-2025-anatomy/oaei-human-mouse/testVoting/NaiveHeuristicsMatrix.txt";
		comparer.writeVotingMatrixToFile(votingMatrix, outPath2, true);
		
		
		
		
		// Now compare to reference to get Sensitivity, Specificity and Youden's index
		String raPath = "/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/reference.rdf";
		Set<MappingObjectStr> referenceMappings = moUtils.readMappingsFromFile(raPath);
		System.out.println("Reference mappings count: " + referenceMappings.size());
		
		Set<MappingObjectStr> heuristicsDiscardedMappings = new HashSet<MappingObjectStr>(newMappings);
		heuristicsDiscardedMappings.removeAll(heuristicsFilteredMappings);
		System.out.println("Mappings accepted by our heuristics filter: " + heuristicsFilteredMappings.size() +
				"Mappings discarded by our heuristic filter: " + heuristicsDiscardedMappings.size());
		
		Hashtable<String, Double> heuStats =moUtils.computeSensitivityAndSpecificity(referenceMappings, heuristicsFilteredMappings, heuristicsDiscardedMappings); 
		System.out.println("\n***Heuristics-filtered mappings***\n");
		System.out.println("\nSensitivity:  " + Math.round(heuStats.get("Sensitivity")*1000d)/1000d + "\t Specificity: " + Math.round(heuStats.get("Specificity")*1000d)/1000d +
				"\t Youden Index: " + Math.round(heuStats.get("YoudenIndex")*1000d)/1000d + "\n");
		
		/*
		 * LLM-oracle annotated mappings
		 */
		
		
		//Upload llm-annotation - using default (with msub we get some extra mappings)
		
		String llmAnnotatedMappingsPath = "/home/valentina/Data/anatomy-composed-llm-annotated/all-composed-minus-llm-default.annotated.tsv";
		Set<MappingObjectStr> llmAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedMappingsPath,"allMappings");
		Set<MappingObjectStr> llmTrueAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedMappingsPath,"onlyTrue");
		Set<MappingObjectStr> llmFalseAnnotatedMappings = new HashSet<MappingObjectStr>(llmAnnotatedMappings);
		llmFalseAnnotatedMappings.removeAll(llmTrueAnnotatedMappings);
		//Sanity check
		System.out.println("Sanity check, there are " + llmAnnotatedMappings.size() + " mappings in the llm-annotated set vs " 
		+ votingMatrix.keySet().size() + " in the original new composed mappings");
		
		// is newMappings subset of llm-annotation?
		//TODO print out something
		ProcessComposedMappings sanityChecker = new ProcessComposedMappings();
		Set<MappingObjectStr> intersectionA = sanityChecker.mappingSetSubtraction(newMappings, llmAnnotatedMappings);
		Set<MappingObjectStr> intersectionB = sanityChecker.mappingSetSubtraction(llmAnnotatedMappings, newMappings);
		
		Hashtable<String, Double> llmStats =moUtils.computeSensitivityAndSpecificity(referenceMappings, llmTrueAnnotatedMappings, llmFalseAnnotatedMappings); 
		System.out.println("\n***LLM-annotated mappings***\n");
		System.out.println("\nSensitivity:  " + Math.round(llmStats.get("Sensitivity")*1000d)/1000d + "\t Specificity: " + Math.round(llmStats.get("Specificity")*1000d)/1000d +
				"\t Youden Index: " + Math.round(llmStats.get("YoudenIndex")*1000d)/1000d + "\n");
		
	
}}

	
	
	
	