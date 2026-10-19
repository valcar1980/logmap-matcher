/*
 * Run the heuristics previously devised as part of LogMapBio to select "reliable" mappings from ontology
 *  alignment.
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import uk.ac.ox.krr.logmap2.Parameters;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;



public class ApplyHeuristicsToMappings{
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
	
	public static void saveExperimentDataLineageToFile(String onto1, String onto2, String mappingPath, String moMappingFolderPath,
			String votingMatrixPath, String HeuristicsResultsPath, String HeuristicsMappingPath) {
		//Create timestamp
		
        // Get the current timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd_hh:mm:ss");
        String timestamp = dateFormat.format(new Date());
		//Create file
		
		// Folder of HeuristicsMappingPath
		String outName = "track_heuristics_mappings_lineage_" + timestamp + ".txt";
		String outPath = new File(HeuristicsMappingPath).getParent();
		outPath = new File(outPath,outName).toString();
		System.out.println(outPath);
		
		try {
		FileWriter writer = new FileWriter(new File(outPath));

		String header = "###  Experiment details ###\n\n";
		writer.write(header);
		
		String input_body = "Input\n-----\n" +
		"source-ontology: " + onto1 + "\n" +
		"target-ontology: " + onto2 + "\n" +
		"input-mappings: " + mappingPath + "\n" +
		"mediating-ontologies-store: " + moMappingFolderPath + "\n" +
		"\n";
		
		writer.write(input_body);
		
		String output_body = "Output\n------\n" +
		"filtered-mappings: " + HeuristicsMappingPath +
		"voting-matrix: " +votingMatrixPath + "\n" + 
		"heuristics-flagging-results: " + HeuristicsResultsPath + "\n" +
		"\n";
		
		writer.write(output_body);
		
		
		writer.flush();
		writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		
	}
	
	
	private static void printExperimentStatsToScreen(Set<MappingObjectStr> newMappings, 
			Set<MappingObjectStr> heuristicsFilteredMappings, Set<MappingObjectStr> heuristicsDiscardedMappings) {
		System.out.println("\n\n***Sanity Checks***\n");
		System.out.println("There are " + newMappings.size() + " new mappings (i.e. composed minus logmap-llm) as baseline");
		System.out.println("Mappings accepted by our heuristics filter: " + heuristicsFilteredMappings.size() +
				"\nMappings discarded by our heuristic filter: " + heuristicsDiscardedMappings.size());
		
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
		String onto1 ="/home/valentina/Data/OAEI-input/largebio/snomed-nci/oaei_SNOMED_extended_overlapping_fma_nci.owl";
		String onto2 = "/home/valentina/Data/OAEI-input/largebio/snomed-nci/oaei_NCI_whole_ontology.owl";
		String mappingPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/composed-mappings-for-llm-annotation/all-composed-minus-llm-mutualsub.txt";
		String moMappingFolderPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/store-composed-mappings/";

		/*
		 * Output paths
		 */
		String baseOutPath = "/home/valentina/Data/OAEI_results_bioml2025_largebio2021/largebio/oaei-snomed-nci/heuristics-filtered-mappings/";
		String votingMatrixPath = baseOutPath + "votingMatrix.txt";
		String HeuristicsResultsPath = baseOutPath + "NaiveHeuristicsMatrix.txt";
		String HeuristicsMappingPath = baseOutPath + "HeuristicsFilteredMappings";

		
		File baseOutFolder = new File(baseOutPath);
		if(!baseOutFolder.exists()) baseOutFolder.mkdir();


		/*
		 * More initialisations
		 */
		
		Set<MappingObjectStr> newMappings = moUtils.readMappingsFromFile(mappingPath);
		File moMappingFolder = new File(moMappingFolderPath);

		
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
			
		
		comparer.writeVotingMatrixToFile(votingMatrix, votingMatrixPath, false);
		

		
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
		comparer.writeVotingMatrixToFile(votingMatrix, HeuristicsMappingPath, true);
		
		moUtils.saveOntologyMappings(heuristicsFilteredMappings, HeuristicsMappingPath, onto1, onto2);
		Set<MappingObjectStr> heuristicsDiscardedMappings = new HashSet<MappingObjectStr>(newMappings);
		heuristicsDiscardedMappings.removeAll(heuristicsFilteredMappings);


		printExperimentStatsToScreen(newMappings, heuristicsFilteredMappings, heuristicsDiscardedMappings);
		saveExperimentDataLineageToFile(onto1, onto2, mappingPath, moMappingFolderPath,
				votingMatrixPath, HeuristicsResultsPath, HeuristicsMappingPath);
	
}}

	
	
	
	