package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
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
	public String referenceMapsPath;
	public String sourceToTargetPath;
	public String simpleMappingsPath;
	public String composedMappingsPath;
	public String newUniqueMappingsPath;
	public String resultsLogmapBioLLMPath;
	public String logmapLLMDefaultPath;
	public String logmapLLMMSubPath;
	public String annotatedComposedLLMDefaultPath;
	public String annotatedComposedLLMMSubtPath;
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
	        
	        // Some optionals
	        try {
	        referenceMapsPath = jsonNode.get("referenceMappingsFullPath").asText();
	        } catch (Exception e) {
	        	System.out.println("Reading config, no path to reference mappins provided, setting to null.");
	        	referenceMapsPath = null;
	        }
	        
	        try {
		        logmapLLMDefaultPath = jsonNode.get("logmapLLMDefaultPath").asText();
		        } catch (Exception e) {
		        	System.out.println("Reading config, no path to logmap LLM results (Default), setting to null.");
		        	logmapLLMDefaultPath = null;
		        }
	        
	        
	        try {
	        	logmapLLMMSubPath = jsonNode.get("logmapLLMMSubPath").asText();
		        } catch (Exception e) {
		        	System.out.println("Reading config, no path to logmap LLM results (Mutual Subsumption), setting to null.");
		        	logmapLLMMSubPath = null;
		        }
	        
	        // Composed mappings annotated via LLM
	        try {
	        	annotatedComposedLLMDefaultPath = jsonNode.get("annotatedComposedLLMDefaultPath").asText();
		        } catch (Exception e) {
		        	System.out.println("Reading config, no path to llm(default)-annotated composed mappings, setting to null.");
		        	annotatedComposedLLMDefaultPath = null;
		        }
	        
	        
	        try {
	        	annotatedComposedLLMMSubtPath = jsonNode.get("annotatedComposedLLMMSubtPath").asText();
		        } catch (Exception e) {
		        	System.out.println("Reading config, no path to llm(mutual subsumption)-annotated composed mappings, setting to null.");
		        	annotatedComposedLLMMSubtPath = null;
		        }
	        
	        // Mediating Ontologies settings
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
	
	
	

	
	
	public void saveListMediatingOntolgies(List<String> selectedMediatingOntologies, 
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
		
		this.resultsLogmapBioLLMPath = parentPath + "store-logmapBioLLM-results";
		createSubDirectory(this.resultsLogmapBioLLMPath);



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
	
	/*
	 * 
	 * Function to compute all statistics
	 * 
	 * 
	 */
	

	public static Hashtable<String, Integer> countTrueFalsePosAndFalseNeg(Set<MappingObjectStr> referenceMappings, Set<MappingObjectStr> testMappings) {
		
		int truePos;
		int falsePos;
		int falseNeg;
		Set<MappingObjectStr> intersection = new HashSet<MappingObjectStr>(referenceMappings);
		intersection.retainAll(testMappings);
		truePos = intersection.size();
		falsePos = testMappings.size() - truePos;
		falseNeg = referenceMappings.size()-truePos;
		
		Hashtable<String, Integer> countStats = new Hashtable<>();
		countStats.put("TP", truePos);
		countStats.put("FP", falsePos);
		countStats.put("FN", falseNeg);

		
		return countStats;
	}
	
	
	public static double computePrecisionFromSets(Set<MappingObjectStr> referenceMappings, Set<MappingObjectStr> testMappings ) {
		double precision;
		Set<MappingObjectStr> intersection = new HashSet<MappingObjectStr>(referenceMappings);
		
		intersection.retainAll(testMappings);

		if (intersection.isEmpty()) {
			System.out.println("Intersection is empty, cannot compute Precision, setting to zero by default");
			precision = 0;
		}
		else {
		
		precision = ((double) intersection.size())/ ((double) testMappings.size());
		//System.out.println(precision);
		}
		
		return (double)Math.round(precision*1000d)/1000d;
		
	}
	
	public static double computePrecisionFromCounts(int truePos, int falsePos) {
		double precision;
		
		if (truePos + falsePos > 0) {
			precision = ((double)truePos) / ((double)(truePos + falsePos));
		}
		else {
			System.out.println("Cannot compute Precision, division undefined (TP + FP is zero), setting to zero by default");
			precision = 0;
			
		}
		return (double)Math.round(precision*1000d)/1000d;
	}
	
	
	public static double computeRecallFromSets(Set<MappingObjectStr> referenceMappings, Set<MappingObjectStr> testMappings) {
		
		double recall;
		Set<MappingObjectStr> intersection = new HashSet<MappingObjectStr>(referenceMappings);
		
		intersection.retainAll(testMappings);
		
		if (intersection.isEmpty()) {
			System.out.println("Intersection is empty, cannot compute Recall, setting to zero by default");
			recall = 0;
		}
		else {
		recall = ((double) intersection.size()) /((double) referenceMappings.size());
		//System.out.println(recall);
		}
 
		return (double)Math.round(recall*1000d)/1000d;
		
		
	}
	
	
	public static Double computeRecallFromCounts(int truePos, int falseNeg) {
		Double recall;
		
		if (truePos + falseNeg > 0) {
			recall = ((double)truePos) / ((double)(truePos + falseNeg));
		}
		else {
			System.out.println("Cannot compute Recall, division undefined (TP + FN is zero), setting to zero by default");
			recall = null;
			
		}
		return (double)Math.round(recall*1000d)/1000d;
	}
	
	
	public static double computeF1score(double precision, double recall) {
		double f1score;
		if ((precision + recall)==0) {
			System.out.println("Both precision and recall are zero, cannot compute F1, setting it to zero by default");
			f1score = 0;
		}
		f1score = 2* (precision*recall)/(precision + recall);

		return (double)Math.round(f1score*1000d)/1000d;
	}
	
	
	
	public static void main(String[] args) {
		
		System.out.println("\n\nYou have chosen to run a utils class, displaying all available methods\n\n");
		try {
			Class<MediatingOntologiesUtils> thisClass = MediatingOntologiesUtils.class;
            Method[] methods = thisClass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                System.out.println("\n * " + methods[i].toString());
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
		
	}
	
}
