package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.bioportal.MediatingOntologyExtractor;
import uk.ac.ox.krr.logmap2.io.OutPutFilesManager;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class MediatingOntologiesUtils {
	public String parentPath;
	public String sourceOntoPath;
	public String targetOntoPath;
	public String localOntoRepoPath;
	public String sourceToTargetPath;
	public String simpleMappingsPath;
	public String composedMappingsPath;
	public String newUniqueMappingsPath;
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
	
	
	/**
	 * Given Logmap mappings between two ontologies, it uses the provided representative labels 
	 * to identify the name of the mediating ontologies.
	 * @param onto_mappings
	 * @return List<String> of ontology labels from the mediating ontologies found
	 */
	public List<String> extractMediatingOntologyList(LogMap2_Matcher onto_mappings) {
		
		Set<String> representative_labels = onto_mappings.getRepresentativeLabelsForMappings();
	
		MediatingOntologyExtractor mo_extract = new MediatingOntologyExtractor(representative_labels);
	
		List<String> mediating_ontologies = mo_extract.getSelectedMediatingOntologies();
		return mediating_ontologies;
	}
	
	
	

	
	
	public void saveListMediatingOntolgies(boolean saveList, List<String> selectedMediatingOntologies, 
			String filePath){
		if (selectedMediatingOntologies.size() < 1) {
			System.out.println("No mediating ontologies found");

		} else {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) 
	        {
	            for (int i = 0; i < selectedMediatingOntologies.size(); i++) {
	                writer.write(selectedMediatingOntologies.get(i));
	                writer.newLine(); 
	            }
	            System.out.println("ArrayList written to file successfully.");
	        } catch (IOException e) {
	            e.printStackTrace();
			
			}
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
		if (!dir.exists()) dir.mkdir();
		
	}
	public void createSubDirectoriesFromParent() {
				
		this.sourceToTargetPath = parentPath + "store-source-target/";
		createSubDirectory(this.sourceToTargetPath);
			
		this.simpleMappingsPath = parentPath + "store-simple-mappings/";
		createSubDirectory(this.simpleMappingsPath);

		this.composedMappingsPath = parentPath + "store-composed-mappings/";
		createSubDirectory(this.composedMappingsPath);
		
		this.newUniqueMappingsPath = parentPath + "store-unique-mappings/";
		createSubDirectory(this.newUniqueMappingsPath);
		


	}
	
	/*
	 * Save to file
	 */
	
	/**
	 * 
	 * @param Mappings
	 * @param mappingPath String full path + name of the file, the function only adds the file extension
	 * @param onto1_iri
	 * @param onto2_iri
	 * @return
	 */
	
	public String saveOntologyMappings(Set<MappingObjectStr> Mappings, String mappingPath,
			String onto1_iri, String onto2_iri ) {
		
		OutPutFilesManager mapSaver = new OutPutFilesManager();
		String path2file = null;
		// 5 = AllFlatFormats
		try {
			mapSaver.createOutFiles(mappingPath, 5, onto1_iri, onto2_iri);
			mapSaver.addMappings(Mappings);
			mapSaver.closeAndSaveFiles();
			path2file = mappingPath;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Failed to save mappings, returning null");
		}
		return path2file;
	}
	
	
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
	
	public void printMediatingOntologies(List<String> selectedMediatingOntologies) {

		if (selectedMediatingOntologies.size() < 1) {
			System.out.println("No mediating ontologies found");

		} else {
			for (int i = 0; i < selectedMediatingOntologies.size(); i++) {
				System.out.println(selectedMediatingOntologies.get(i));
			}
		}
	}
	
	public void main(String[] args) {
		
	}
	
}
