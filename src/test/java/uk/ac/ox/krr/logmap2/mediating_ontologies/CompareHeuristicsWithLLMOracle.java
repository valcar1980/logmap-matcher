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
	
	
	public Hashtable<String, Double> computeSensitivityAndSpecificity(Set<MappingObjectStr> referenceMappings, 
			Set<MappingObjectStr> testPosMappings, Set<MappingObjectStr> testNegMappings) {
		double sensitivity;
		double specificity;
		double youdenIndex;
		
		int truePos; Set<MappingObjectStr> truePosSet = new HashSet<MappingObjectStr>();
		int falsePos;Set<MappingObjectStr> falsePosSet = new HashSet<MappingObjectStr>();
		int falseNeg;Set<MappingObjectStr> falseNegSet = new HashSet<MappingObjectStr>();
		int trueNeg;Set<MappingObjectStr> trueNegSet = new HashSet<MappingObjectStr>();
		
		for(MappingObjectStr pmap: testPosMappings) {
			if (referenceMappings.contains(pmap)) truePosSet.add(pmap);
			else falsePosSet.add(pmap);
			
		for (MappingObjectStr nmap: testNegMappings) {
			if (referenceMappings.contains(nmap)) falseNegSet.add(nmap);
			else trueNegSet.add(nmap);
			
		}
		}
		
		truePos = truePosSet.size();//test says yes and reference says yes
		falsePos = falsePosSet.size();//test says yes but reference says no(i.e. map is not present in the reference)
		falseNeg = falseNegSet.size();// test says no but reference says yes
 		trueNeg = trueNegSet.size();//test say no and reference say no (i.e. map is not present in the reference)
		
		sensitivity = (double)(truePos)/ ((double)(truePos + falseNeg)); // probability of our test to detect a mapping when the mapping is in the reference
		specificity = (double)(trueNeg)/ ((double)(trueNeg + falsePos)); // probability of our test to discard a mapping when the mapping is not in the reference
		youdenIndex = sensitivity + specificity -1.0;
		Hashtable<String, Double> diagnosticStats = new Hashtable<String, Double> ();
		diagnosticStats.put("Sensitivity", sensitivity);
		diagnosticStats.put("Specificity", specificity);
		diagnosticStats.put("YoudenIndex", youdenIndex);

		return diagnosticStats;
	}
	
	
	
	
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
		
		/*
		 * Initialisations
		 */
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		ProcessComposedMappings cheekySwapper = new ProcessComposedMappings();
		
		/*
		 * Input data
		 */
		
		String mappingPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/composed-mappings-for-llm-annotation/all-composed-minus-llm-mutualsub.txt";
		String moMappingFolderPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/store-composed-mappings/";
		String llmAnnotatedMappingsPath = "/home/valentina/Data/composed-llm-annotated/logmap-composed-mappings-largebio-2021_ANNOTATED/snomed-nci/all-composed-minus-llm-mutualsub.annotated.tsv";
		String raPath = "/home/valentina/Data/OAEI-input/oaei-2025-input/largebio/snomed-nci/oaei_SNOMED2NCI_UMLS_mappings_with_flagged_repairs.rdf";
		/*
		 * Output paths
		 */
		String baseOutPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/testVoting/";
		String outPath = baseOutPath + "votingMatrix.txt";
		String outPath2 = baseOutPath + "NaiveHeuristicsMatrix.txt";

		
		File baseOutFolder = new File(baseOutPath);
		if(!baseOutFolder.exists()) baseOutFolder.mkdir();


		/*
		 * More initialisations
		 */
		
		Set<MappingObjectStr> newMappings = moUtils.readMappingsFromFile(mappingPath);
		File moMappingFolder = new File(moMappingFolderPath);
		Set<MappingObjectStr> referenceMappings = moUtils.readMappingsFromFile(raPath);
		
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
			Boolean sameMappingOrder;

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
		comparer.writeVotingMatrixToFile(votingMatrix, outPath2, true);
		
		
		// Now compare to reference to get Sensitivity, Specificity and Youden's index

		System.out.println("Reference mappings count: " + referenceMappings.size());
		
		//Get real positives and real negatives
		CompareToReference experiment = new CompareToReference();
		Hashtable<String, Integer> countStats = experiment.computeRealPositivesAndNegatives(referenceMappings, newMappings);
		
		
		Set<MappingObjectStr> heuristicsDiscardedMappings = new HashSet<MappingObjectStr>(newMappings);
		heuristicsDiscardedMappings.removeAll(heuristicsFilteredMappings);

		
		Hashtable<String, Double> heuStats =comparer.computeSensitivityAndSpecificity(referenceMappings, heuristicsFilteredMappings, heuristicsDiscardedMappings); 
		
		
		/*
		 * LLM-oracle annotated mappings
		 */
		
		Set<MappingObjectStr> llmAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedMappingsPath,"allMappings");
		Set<MappingObjectStr> llmTrueAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedMappingsPath,"onlyTrue");
		Set<MappingObjectStr> llmFalseAnnotatedMappings = new HashSet<MappingObjectStr>(llmAnnotatedMappings);
		llmFalseAnnotatedMappings.removeAll(llmTrueAnnotatedMappings);
		//Sanity check

		
		// is newMappings subset of llm-annotation?
		//TODO print out something
		ProcessComposedMappings sanityChecker = new ProcessComposedMappings();
		sanityChecker.mappingSetSubtraction(newMappings, llmAnnotatedMappings);
		sanityChecker.mappingSetSubtraction(llmAnnotatedMappings, newMappings);
		
		Hashtable<String, Double> llmStats =comparer.computeSensitivityAndSpecificity(referenceMappings, llmTrueAnnotatedMappings, llmFalseAnnotatedMappings); 
		
		/*
		 * Print results to screen
		 */
		System.out.println("\n\n***Sanity Checks***\n");
		System.out.println("There are " + newMappings.size() + " new mappings (i.e. composed minus logmap-llm) as baseline");
		System.out.println("Mappings accepted by our heuristics filter: " + heuristicsFilteredMappings.size() +
				"\nMappings discarded by our heuristic filter: " + heuristicsDiscardedMappings.size());
		System.out.println("There are " + llmAnnotatedMappings.size() + " mappings in the llm-annotated set vs " 
		+ votingMatrix.keySet().size() + " in the original new composed mappings");
		System.out.println("Mappings accepted by our llm-msub annotation: " + llmTrueAnnotatedMappings.size() +
				"\nMappings discarded by our llm-msub annotation: " + llmFalseAnnotatedMappings.size());
		
		System.out.println("\n***New mappings counts***\n");
		System.out.println("Real Positives: " + countStats.get("(Real)Positives") + "\t Real Negatives: " + countStats.get("(Real)Negatives"));
		System.out.println("\n***Heuristics-filtered mappings***\n");
		System.out.println("Sensitivity:  " + Math.round(heuStats.get("Sensitivity")*1000d)/1000d + "\t Specificity: " + Math.round(heuStats.get("Specificity")*1000d)/1000d +
				"\t Youden Index: " + Math.round(heuStats.get("YoudenIndex")*1000d)/1000d + "\n");
		System.out.println("\n***LLM-annotated mappings***\n");
		System.out.println("Sensitivity:  " + Math.round(llmStats.get("Sensitivity")*1000d)/1000d + "\t Specificity: " + Math.round(llmStats.get("Specificity")*1000d)/1000d +
				"\t Youden Index: " + Math.round(llmStats.get("YoudenIndex")*1000d)/1000d + "\n");
		
	
}}

	
	
	
	