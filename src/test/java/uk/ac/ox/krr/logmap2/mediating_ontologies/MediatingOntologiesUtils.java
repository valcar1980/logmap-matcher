package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class MediatingOntologiesUtils {
	public String parentPath;
	public String sourceOntoPath;
	public String targetOntoPath;
	public String localOntoRepoPath;
	public String sourceToTargetPath;
	public String simpleMappingsPath;
	public String composedMappingsPath;
	public Boolean overrideMOnum;
	public Integer maxMONum;
	
	public MediatingOntologiesUtils(){
		
	}
	
	/*
	 * Read from program call arguments
	 */
	
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
	
	/*
	 * Load from file
	 */
	
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
	        localOntoRepoPath = jsonNode.get("repoMediatingOntologiesFullPath").asText();
	        overrideMOnum = jsonNode.get("overrideMaxMediatingOntologies").asBoolean();
	        if (overrideMOnum == true) maxMONum = jsonNode.get("maxMediatingOntologies").asInt();
	        
	        if (new File(sourceOntoPath).exists() && new File(targetOntoPath).exists()) {
	        System.out.println("Found files for the ontologies provided as source " + sourceOntoPath 
	        		+ "\n and as target " +targetOntoPath);
	        }
	        else {
	        	System.out.println("Check your configs, either source or target onto are missing!");
	        }
	        
	        File f = new File(localOntoRepoPath);
			if ( f.isDirectory() == false) {
	        	System.out.println("Check your configs, your repo of mediating ontologies is not found.");
				
			}
		}
	} catch (Exception e) {
		System.out.println("We could not read one of the configs...Check them up!");
		e.printStackTrace();
		System.out.println(jsonPath);
	}
	}
	
	/*
	 * Create folder structures
	 */
	
	/**
	 *  Set sub-folders inside parentPath for further processing. In this method, the mediating ontologies are expected
	 * to come from a folder external to the parentPath. This method is recommended, to avoid downloading multiple times
	 * the same ontologies.
	 * @param parentPath
	 * @param localOntoRepo
	 */
	
	private void createSubDirectory(String path) {
		File dir = new File(path);
		if (!dir.exists()) dir.mkdirs();
		
	}
	public void createSubDirectoriesFromParent() {
				
		this.sourceToTargetPath = parentPath + "store-source-target/";
		createSubDirectory(this.sourceToTargetPath);
			
		this.simpleMappingsPath = parentPath + "store-simple-mappings/";
		createSubDirectory(this.simpleMappingsPath);

		this.composedMappingsPath = parentPath + "store-composed-mappings/";
		createSubDirectory(this.composedMappingsPath);

	}
	
	/*
	 * Save to file
	 */
	
	
	
	
	/*
	 * Print to screen
	 */
	
	public static void printOntologyMappings(LogMap2_Matcher onto_mapping) {
		Set<MappingObjectStr> logmap2_mappings = onto_mapping.getLogmap2_Mappings();
		for (MappingObjectStr mapping : logmap2_mappings) {
			System.out.println("\t Mapping: ");
			System.out.println("Entity from ontology 1 \t" + mapping.getIRIStrEnt1());
			System.out.println("Entity from ontology 2 \t" + mapping.getIRIStrEnt2());
			System.out.println("Confidence in the mapping \t" + mapping.getConfidence());

			// MappingObjectStr.EQ or MappingObjectStr.SUB or MappingObjectStr.SUP
			System.out.println("Mapping direction \t" + mapping.getMappingDirection()); // Utilities.EQ;

			// MappingObjectStr.CLASSES or MappingObjectStr.OBJECTPROPERTIES or
			// MappingObjectStr.DATAPROPERTIES or MappingObjectStr.INSTANCES
			System.out.println("Mapping type \t" + mapping.getTypeOfMapping());

		}
	}
	
	public void main(String[] args) {
		
	}
	
}
