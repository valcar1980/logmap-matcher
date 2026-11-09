package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;
import uk.ac.ox.krr.logmap2.LogMap3_RepairFacility;
import uk.ac.ox.krr.logmap2.OntologyLoader;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class MakeLogMapBioMappings implements ExperimentListInterface{
	MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
	public Map<String,JsonNode> experimentSet;
	
	public MakeLogMapBioMappings(){
		experimentSet = new HashMap<String,JsonNode>();
	}
	
	public void readExperimentList(String experimentPath) {
		// TODO Auto-generated method stub
		File jsonConfig = new File(experimentPath);
		try {
			if (jsonConfig.exists() == false) {
				System.out.println("Couldn't find config JSON in parent folder " + experimentPath);
				return;
			} else {

				System.out.println("Found config JSON in parent folder " + experimentPath);
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(jsonConfig);

				if (rootNode.isObject()) {
					Iterator<String> fieldNames = rootNode.fieldNames();
					while (fieldNames.hasNext()) {
						String experimentName = fieldNames.next();
		                JsonNode innerNode = rootNode.get(experimentName);

						if(innerNode.isObject())this.experimentSet.put(experimentName,innerNode);
						
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String,String> readExperimentConfigs(JsonNode experimentConfig) {
		HashMap<String,String> singleExperimentConfig = new HashMap<String,String>();
		String experimentType = null;
		String sourcePath = null;
		String targetPath = null;
		if(!experimentConfig.isObject()) {
			System.out.println("Incorrect json element, existing with null");
			
		}else {
			experimentType = experimentConfig.path("experiment_type").asText();
			sourcePath   = experimentConfig.path("source_onto_path").asText();
	        targetPath   = experimentConfig.path("target_onto_path").asText();
	        
			singleExperimentConfig.put("experiment_type", experimentType);
			singleExperimentConfig.put("source_onto_path", sourcePath);
			singleExperimentConfig.put("target_onto_path", targetPath );
		
			if(experimentType.equals("logmapbio")) {
				System.out.println("This is a logmapbio experiment");
				System.out.println("\t source: " + sourcePath + "\n\t target: " + targetPath);	
				String logMapPath = experimentConfig.path("logmap_mappings_path").asText();
				String moMapPath = experimentConfig.path("unique_mo_mappings_path").asText();
				String moComposedFolderPath = experimentConfig.path("mo_composed_mappings_directory").asText();
				
				singleExperimentConfig.put("logmap_mappings_path", logMapPath );
				singleExperimentConfig.put("unique_mo_mappings_path", moMapPath );
				singleExperimentConfig.put("mo_composed_mappings_directory", moComposedFolderPath );
				
			}
		}
		
		return singleExperimentConfig;
		
	}
	
	public void printExperimentList() {
		System.out.println("There are " + this.experimentSet.size() + " experiments in the list:\n");
		Integer countExp = 0;
		for(String expKey:this.experimentSet.keySet()) {
			countExp++;
			System.out.println(countExp.toString() + ": " + expKey);
		}
	}
	
	/**
	 * Computes LogMapBio mappings via the Mediating Ontologies and applying heuristics to filter
	 * @param singleExperimentConfig
	 * @param parentFolderPath folder where you want all the mappings and logs to be stored
	 * @return
	 */
	
	public Set<MappingObjectStr> runLogMapBioExperiment(HashMap<String,String> singleExperimentConfig, String parentFolderPath) {
		System.out.println("I am running a LogMapBio experiment");
		Set<MappingObjectStr> logMapBioMaps;
		try {
			String sourceOntoPath = singleExperimentConfig.get("source_onto_path");
			String targetOntoPath = singleExperimentConfig.get("target_onto_path");
			// get logmap mappings
			String logMapPath = singleExperimentConfig.get("logmap_mappings_path");
			System.out.println("logmap path " + logMapPath);
			Set<MappingObjectStr> logMaps = moUtils.readMappingsFromFile(logMapPath);
			// get mo unique mappings (minus logmap)
			String moMapsPath = singleExperimentConfig.get("unique_mo_mappings_path");
			System.out.println("mo maps path " + moMapsPath);
			Set<MappingObjectStr> newMappings = moUtils.readMappingsFromFile(moMapsPath);
			// get MO folder with individual composed mappings sets
			String moComposedFolderPath =  singleExperimentConfig.get("mo_composed_mappings_directory");
			
			// apply heuristics to unique mappings
			ApplyHeuristicsToMappings heuristical = new ApplyHeuristicsToMappings(sourceOntoPath,targetOntoPath, moMapsPath,moComposedFolderPath,parentFolderPath);
			heuristical.runFullThing();
			Boolean displayPassThreshold = true;
			heuristical.writeHeuristicsVotingMatrixToFile(displayPassThreshold);
			Boolean saveDiscarded = true;
			heuristical.saveHeuristicsMappings(saveDiscarded);		
			heuristical.printExperimentStatsToScreen();
			heuristical.saveExperimentDataLineageToFile();
			
			
			// apply repair to unique mappings	
			System.out.println("Starting repair of new mappings");
			OWLOntology sourceOnto = new OntologyLoader("file:" + sourceOntoPath).getOWLOntology();
			OWLOntology targetOnto = new OntologyLoader("file:" + targetOntoPath).getOWLOntology();
			LogMap3_RepairFacility repairOperator = new LogMap3_RepairFacility(sourceOnto,targetOnto,logMaps,newMappings);
			Set<MappingObjectStr> newMappings_repaired = repairOperator.getCleanMappings();
			System.out.println("Completed repair of new mappings");
			
						// combine logmap and new repaired mappings
			logMapBioMaps = new HashSet<MappingObjectStr>(newMappings_repaired);
			logMapBioMaps.addAll(logMaps);
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	
		return logMapBioMaps;
	}

	public static void main(String[] args) {
		//String experimentPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/experiment_logmapbio_mappings.json";
		String experimentPath = args[0];
		// String parentFolderPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/mappings/";
		String parentFolderPath = args[1];
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		MakeLogMapBioMappings fullExperimentSet = new MakeLogMapBioMappings();
		fullExperimentSet.readExperimentList(experimentPath);
		fullExperimentSet.printExperimentList();
		Map<String,JsonNode> tmp = fullExperimentSet.experimentSet;
		for( String exp:tmp.keySet()) {
			JsonNode tmpExp = tmp.get(exp);
			File checkFolder = new File(parentFolderPath,exp);
			if (checkFolder.exists() && checkFolder.isDirectory()) {
				System.out.println("Folder Already exists for experiment "+ exp);
			}
			
				
				System.out.println("New experiment: " + exp);
			HashMap<String,String> singleExperimentConfig = fullExperimentSet.readExperimentConfigs(tmpExp);
			String sourceOntoPath = singleExperimentConfig.get("source_onto_path");
			String targetOntoPath = singleExperimentConfig.get("target_onto_path");
			if(singleExperimentConfig.get("experiment_type").equals("logmapbio")) {
				String outputDirPath = new File(parentFolderPath, exp).toString();
				File outputFilePath = new File(outputDirPath, "logmapbiomaps.txt");
//				if(outputFilePath.exists() && outputFilePath.isFile()) {
//					System.out.println("Found maps" + outputFilePath.toString() + "\t skipping");
//					continue;
//				}else {
					Set<MappingObjectStr> logMapBioMaps = fullExperimentSet.runLogMapBioExperiment(singleExperimentConfig,outputDirPath);
						
						System.out.println("Final mappings count for LogMapBio " + logMapBioMaps.size());
						System.out.println("Saving logMapBioMaps in file " + outputFilePath.toString());
						moUtils.saveOntologyMappings(logMapBioMaps, outputFilePath.toString(), sourceOntoPath, targetOntoPath);
				
				}
		}
	}
}

	
