package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MakeLogMapBioMappings{
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
		String sourcePath;
		String targetPath;
		if(!experimentConfig.isObject()) {
			System.out.println("Incorrect json element, existing with null");
			
		}else {
			experimentType = experimentConfig.path("experiment_type").asText();
			sourcePath   = experimentConfig.path("source_onto_path").asText();
	        targetPath   = experimentConfig.path("target_onto_path").asText();
	        //System.out.println("\t source: " + sourcePath + "\n\t target: " + targetPath);
		
		
		
			if(experimentType.equals("logmapbio")) {
				System.out.println("This is a logmapbio experiment");
				System.out.println("\t source: " + sourcePath + "\n\t target: " + targetPath);	
				
			}
		}
		
		singleExperimentConfig.put("experiment_type", experimentType);
		
		
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
	
	
	public void runLogMapBioExperiment() {
		System.out.println("I am running a LogMapBio experiment");
	}

	public static void main(String[] args) {
		String experimentPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/experiment_logmapbio_mappings.json";
		String parentFolderPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/mappings/";
		MakeLogMapBioMappings fullExperimentSet = new MakeLogMapBioMappings();
		fullExperimentSet.readExperimentList(experimentPath);
		fullExperimentSet.printExperimentList();
		Map<String,JsonNode> tmp = fullExperimentSet.experimentSet;
		for( String exp:tmp.keySet()) {
			JsonNode tmpExp = tmp.get(exp);
			System.out.println("Experiment: " + exp);
			HashMap<String,String> singleExperimentConfig = fullExperimentSet.readExperimentConfigs(tmpExp);
			
			if(singleExperimentConfig.get("experiment_type").equals("logmapbio")) {
				fullExperimentSet.runLogMapBioExperiment();
			}
		}
		
	}
	
}