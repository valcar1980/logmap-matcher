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
	MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
	ProcessComposedMappings cheekySwapper = new ProcessComposedMappings();
	final double confidenceLowThreshold = Parameters.confidence_composed_mappings2; 
	final double confidenceHighThreshold = Parameters.confidence_composed_mappings1;
	HashMap<MappingObjectStr, MappingMetadata> heuristicsVotingMatrix;
	Set<MappingObjectStr> newMappings;
	Set<MappingObjectStr> heuristicsFilteredMappings;
	Set<MappingObjectStr> heuristicsDiscardedMappings;
	String sourceOntoPath;
	String targetOntoPath;
	String newMappingsPath;
	String moMappingsFolderPath;
	String baseOutputPath;
	String heuristicsVotingMatrixPath;
	String heuristicsFilteredMappingsPath;
	String heursticsDiscardedMappingsPath;

	public ApplyHeuristicsToMappings(String onto1, String onto2, String newMaps, String moDir, String baseDir) {
		
		sourceOntoPath = onto1;
		targetOntoPath = onto2;
		newMappingsPath = newMaps;
		moMappingsFolderPath = moDir;
		baseOutputPath = baseDir;
		

		
		heuristicsVotingMatrix = new HashMap<MappingObjectStr, MappingMetadata>();
		try {
			newMappings = moUtils.readMappingsFromFile(newMappingsPath);
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		File base = new File(baseOutputPath);
		if (!base.exists()) {
			base.mkdir();

		} else if (base.isDirectory()) {
			heuristicsVotingMatrixPath = new File(base, "mappings_with_heuristics_voting.txt").toString();
			heuristicsFilteredMappingsPath = new File(base, "heuristics_filtered_mappings").toString();
			heursticsDiscardedMappingsPath = new File(base, "heuristics_discarded_mappings").toString();
		}
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
	 * @param heuristicsVotingMatrix 
	 * @param outPath
	 */
	public void writeHeuristicsVotingMatrixToFile(Boolean displayPassThreshold) {
		File heuristicsVotingMatrixFile = new File(heuristicsVotingMatrixPath);
		BufferedWriter bf = null;

		try {

			// create new BufferedWriter for the output file
			bf = new BufferedWriter(new FileWriter(heuristicsVotingMatrixFile));
			if(displayPassThreshold==false) {
				bf.write("#\t Mapping \t Confidence \t VoteCount \t MediatingOntologies");}
			else {
				bf.write("#\t Mapping \t Confidence \t VoteCount \t passThreshold \t MediatingOntologies");

			}
			bf.newLine();

			for (MappingObjectStr key : heuristicsVotingMatrix.keySet()) {
				// System.out.println(key);

				// put key and value separated by a colon
				bf.write(key + "\t" + heuristicsVotingMatrix.get(key).toString(displayPassThreshold));

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
	
	public void saveExperimentDataLineageToFile() {
		
        // Get the current timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd_hh:mm:ss");
        String timestamp = dateFormat.format(new Date());
		//Create file
		
		// Folder of HeuristicsMappingPath
		String outName = "track_heuristics_mappings_lineage_" + timestamp + ".txt";
		String outPath = new File(baseOutputPath,outName).toString();
		System.out.println(outPath);
		
		try {
		FileWriter writer = new FileWriter(new File(outPath));

		String header = "###  Experiment details ###\n\n";
		writer.write(header);
		
		String input_body = "Input\n-----\n" +
		"source-ontology: " + sourceOntoPath + "\n" +
		"target-ontology: " + targetOntoPath + "\n" +
		"input-mappings: " +  newMappingsPath+ "\n" +
		"mediating-ontologies-store: " + moMappingsFolderPath + "\n" +
		"\n";
		
		writer.write(input_body);
		
		String output_body = "Output\n------\n" +
		"heuristics-mappings-voting-matrix: " + heuristicsVotingMatrixPath + "\n" +
		"heuristics-filtered-mappings: " + heuristicsFilteredMappingsPath + "\n" +
		"heuristics-discarded-mappings: " + heursticsDiscardedMappingsPath+ "\n" +
		"\n";
		
		writer.write(output_body);
		
		writer.flush();
		writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public void printExperimentStatsToScreen() {
		System.out.println("\n\n***Sanity Checks***\n");
		System.out.println("There are " + newMappings.size() + " new mappings (i.e. composed minus logmap-llm) as baseline");
		System.out.println("Mappings accepted by our heuristics filter: " + heuristicsFilteredMappings.size() +
				"\nMappings discarded by our heuristic filter: " + heuristicsDiscardedMappings.size());
		
	}
	
	
	public void runFullThing(){
		
	
		File moMappingFolder = new File(moMappingsFolderPath);

		
		/*
		 * Create the voting matrix, key: mapping, value: [] empty array, which will be filled later
		 */
		
		Set<MappingObjectStr> newMappings = moUtils.readMappingsFromFile(newMappingsPath);
		
		for(MappingObjectStr mapping: newMappings) {
			
			MappingMetadata meta = new MappingMetadata();
			meta.setConfidence(mapping.getConfidence());
			//Assign confidence and initialise the rest
			heuristicsVotingMatrix.put(mapping, meta);
		}
		
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

						MappingMetadata currentMeta = heuristicsVotingMatrix.get(map);
						MappingMetadata updateMeta = new MappingMetadata(currentMeta);
						
						updateMeta.addToVotingCount();
						updateMeta.addToMOList(ontoLabel);
						heuristicsVotingMatrix.put(map, updateMeta);
					}
				}
			}
		}
		
		/*
		 * Apply heuristics to mappings
		 */
		
		// System.out.println("Heuristics based on 2+ votes and confidence >= " + confidenceLowThreshold 
		// + "or 1 vote and confidence >= " + confidenceHighThreshold);
		 heuristicsFilteredMappings = new HashSet<MappingObjectStr>();
		 Integer countPassedThreshold = 0;
		for(HashMap.Entry<MappingObjectStr,MappingMetadata> entry:heuristicsVotingMatrix.entrySet()) {
	        {
	            MappingObjectStr mapping = entry.getKey();
	            MappingMetadata currentTabular = entry.getValue();
	            MappingMetadata updateTabular = new MappingMetadata(currentTabular);
	            
	            //use key and value
	            Boolean passThreshold = applyNaiveHeuristicFilter(currentTabular.getConfidence(), currentTabular.getVotingCount());
	            updateTabular.setPassThreshold(passThreshold);
	            heuristicsVotingMatrix.put(mapping, updateTabular);
	            
	            if(passThreshold == true) {
	            	countPassedThreshold++;
	            	heuristicsFilteredMappings.add(mapping);
	            }
	        }
		}
		//System.out.println("The mappings passing the heuristic threshold are " + countPassedThreshold + " out of " + heuristicsVotingMatrix.size());

		heuristicsDiscardedMappings = new HashSet<MappingObjectStr>(newMappings);
		heuristicsDiscardedMappings.removeAll(heuristicsFilteredMappings);
	
	}
	
	public void saveHeuristicsMappings(Boolean saveDiscarded) {

		moUtils.saveOntologyMappings(heuristicsFilteredMappings, heuristicsFilteredMappingsPath, sourceOntoPath,targetOntoPath);
		if(saveDiscarded == true) {
			moUtils.saveOntologyMappings(heuristicsDiscardedMappings, heursticsDiscardedMappingsPath, sourceOntoPath,targetOntoPath);

		}
	}
	
	public static void main (String[] args) {
		
		/*
		 * Hard-coded input :( //TODO config?
		 */
		String onto1 ="/home/valentina/Data/OAEI-input/oaei-2026-input/bio-ml-ontologies/NCIT-bd7a9a8a75e5-Thesaurus.owl";
		String onto2 = "/home/valentina/Data/OAEI-input/oaei-2026-input/bio-ml-ontologies/DOID-611355c44553-doid.owl";
		// the full set of mappings obtained via the Mediating Ontologies - excluding those already identified by logmap/baseline
		String mappingPath = "/home/valentina/Data/OAEI-output/oaei-2026-bio-ml/oaei-2026-snomed-ncit/config_logmap_bio/composed-mappings-for-llm-annotation-noMondo/all-composed-minus-llm-mutualsub.txt";
		//to retrieve individual MO contribution to the mapping pool
		String moMappingFolderPath = "/home/valentina/Data/OAEI-output/oaei-2026-bio-ml/oaei-2026-snomed-ncit/config_logmap_bio/store-composed-mappings/";
		String baseOutPath = "/home/valentina/Data/tests/test-heu-rep/";


		ApplyHeuristicsToMappings heuristical = new ApplyHeuristicsToMappings(onto1, onto2, mappingPath, moMappingFolderPath,baseOutPath);
		heuristical.runFullThing();
		//System.out.println("The mappings passing the heuristic threshold are " + countPassedThreshold + " out of " + heuristicsVotingMatrix.size());
		Boolean displayPassThreshold = true;
		heuristical.writeHeuristicsVotingMatrixToFile(displayPassThreshold);
		Boolean saveDiscarded = true;
		heuristical.saveHeuristicsMappings(saveDiscarded);		
		heuristical.printExperimentStatsToScreen();
		heuristical.saveExperimentDataLineageToFile();
		
		}



	}	
		

		

	
	
	
	