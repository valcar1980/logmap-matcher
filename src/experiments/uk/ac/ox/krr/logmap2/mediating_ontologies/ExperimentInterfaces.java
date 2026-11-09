package uk.ac.ox.krr.logmap2.mediating_ontologies;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;


interface SingleExperimentInterface {
	
	public void readExperimentConfigs(JsonNode experimentConfig);
	public void loadExperimentInput();
	public void runExperimentPipeline();
	public void storeExperimentOutput();
	public void saveExperimentDataLineageToFile();
	public void printExperimentStatsToScreen();
	public void saveExperimentLogsToFile();
	
}


// when you have a list of experiments 

interface ExperimentListInterface {
	
	public void readExperimentList(String experimentPath);
	public HashMap<String,String> readExperimentConfigs(JsonNode experimentConfig);
	

}

