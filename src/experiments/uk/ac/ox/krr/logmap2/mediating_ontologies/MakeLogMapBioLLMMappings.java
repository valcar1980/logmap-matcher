/*
 * Where we do the work described that combines annotated composed mappings (from mediating ontologies) with mappings obtained by LogmapLLM to produce
 * LogmapBioLLM.
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;
import uk.ac.ox.krr.logmap2.LogMap3_RepairFacility;
import uk.ac.ox.krr.logmap2.io.OutPutFilesManager;
import uk.ac.ox.krr.logmap2.io.ReadFile;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;

//import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;

public class MakeLogMapBioLLMMappings implements ExperimentListInterface {
	private static String repairStrategy;
	MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
	public Map<String,JsonNode> experimentSet;
//	public Map<String,Map<String,Integer>> experimentStats;
//	public Map<String,Integer> statsMap = new HashMap<String,Integer>();
	
	
	public MakeLogMapBioLLMMappings(){
		experimentSet = new HashMap<String,JsonNode>();
//		statsMap.put("totalMapCount0", 0); // all mappings without any repair
//		statsMap.put("totalMapCount1", 0); // all mappings after first repair
//		statsMap.put("totalMapCount2", 0); // all mappings after second repair
//		Map<String,Map<String,Integer>> experimentStats = new HashMap<String,Map<String,Integer>>();
	}
	
	private static void setRepairStrategy(String repairOption) {
		if(repairOption.equals("none")|| repairOption.equals("single") || repairOption.equals("double")) {
		repairStrategy = repairOption;
		}else {
			System.out.println("Incorrect option for repair strategy. Only 'single' and 'double' are available");
		}
	}
	
	
	@Override
	public void readExperimentList(String experimentPath) {
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

						if (innerNode.isObject())
							this.experimentSet.put(experimentName, innerNode);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}



	@Override
	public HashMap<String, String> readExperimentConfigs(JsonNode experimentConfig) {
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
		
			
			/*
			 * 		TODO
					"logmapllm_mappings_path":"",
					"logmallm_setup": "msub2026",
			 */
			if(experimentType.equals("logmapbiollm")) {
				System.out.println("This is a logmapbiollm experiment");
				System.out.println("\t source: " + sourcePath + "\n\t target: " + targetPath);	
				String repairOption = experimentConfig.path("repair_strategy").asText();
				String logMapLLMPath = experimentConfig.path("logmapllm_mappings_path").asText();
				String moMapsAnnotatedPath = experimentConfig.path("mo_composed_mappings_annotated").asText();
				
				
				singleExperimentConfig.put("logmapllm_mappings_path", logMapLLMPath );
				singleExperimentConfig.put("mo_composed_mappings_annotated", moMapsAnnotatedPath );
				singleExperimentConfig.put("repair_strategy", repairOption);
				
			}
		}
		
		return singleExperimentConfig;
		
	}
	

	public Set<MappingObjectStr> runLogMapBioLLMExperiment(HashMap<String,String> singleExperimentConfig, String parentFolderPath){
		
		Set<MappingObjectStr> mappingsLogMapBioLLM;
		MakeLogMapBioLLMMappings setup = new MakeLogMapBioLLMMappings();
		//to store to file mappings
		String outputFilename = parentFolderPath + "logmapbiollm-mappings";
		
		String sourceOntoPath = singleExperimentConfig.get("source_onto_path");
		String targetOntoPath = singleExperimentConfig.get("target_onto_path");
		String repairOption = singleExperimentConfig.get("repair_strategy");
		System.out.println("repair_strategy" + repairOption);
		String logMapLLMPath = singleExperimentConfig.get("logmapllm_mappings_path");
		System.out.println("logmapllm path " + logMapLLMPath);
		String moMapsAnnotatedPath = singleExperimentConfig.get("mo_composed_mappings_annotated");
		System.out.println("mo maps path " + moMapsAnnotatedPath);

		setRepairStrategy(repairOption);
		
		
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		onto_manager.setOntologyLoaderConfiguration(config);

		/*
		 * Load source and target ontologies
		 */

		String source_iri = sourceOntoPath;
		String target_iri = targetOntoPath;
		OWLOntology onto1 = null;
		OWLOntology onto2 = null;

		try {
			System.out.println("Loading source ontology" + source_iri);
			onto1 = onto_manager.loadOntology(IRI.create("file:" + source_iri));
			
		} catch (Exception e) {System.out.println("Failed to load " + source_iri);
		}

		try {
			System.out.println("Loading target ontology " + target_iri);
			onto2 = onto_manager.loadOntology(IRI.create("file:" + target_iri));

		} catch (Exception e) {System.out.println("Failed to load " + target_iri);
		}
		

		/*
		 * Load mappings
		 */
		
		Set<MappingObjectStr> mappingsLogMapLLM = moUtils.readMappingsFromFile(logMapLLMPath);
		Set<MappingObjectStr> newMapsAnnotatedTrue = moUtils.readAnnotatedMappingsFromTSV(moMapsAnnotatedPath, "onlyTrue");
		Integer sum0 = newMapsAnnotatedTrue.size()+mappingsLogMapLLM.size();
		System.out.println("MO mappings are: " + newMapsAnnotatedTrue.size() + "\t LogMapLLM mappings are " + mappingsLogMapLLM.size() +
				"\n Total mapping before repair: " + sum0);		
		/*
		 * First step of repair - only on true-annotated mappings
		 */

		// fixed_mappings(Reference) are logmapLLM
		// mappings2review are MO ones
		System.out.println("\n\n***First repair***");
		LogMap3_RepairFacility maps_repair1 = new LogMap3_RepairFacility(onto1, onto2, mappingsLogMapLLM,
				newMapsAnnotatedTrue);
		Set<MappingObjectStr> LogMapBioLLM_repair1 = maps_repair1.getCleanMappings();
		System.out.println("Clean mappings after first repair: " + LogMapBioLLM_repair1.size());
		Integer sum1 = LogMapBioLLM_repair1.size() + mappingsLogMapLLM.size();
		System.out.println("Total mappings after first repair: " +  sum1);

		// Our set of mappings now contains clean composed + logmap-llm ones
		LogMapBioLLM_repair1.addAll(mappingsLogMapLLM);
		
		
		/*
		 * Second step of repair - apply to all
		 */
		System.out.println("\n\n***Second repair***");
		//Repair all together just in case
		LogMap2_RepairFacility LogMapBioLLM_repair2 = new LogMap2_RepairFacility(
				onto1, 
				onto2, 
				LogMapBioLLM_repair1,
				false, //overlapping
				false, //always optimal?
				false, //satisfiability_check,
				outputFilename);
		
		
		mappingsLogMapBioLLM = LogMapBioLLM_repair2.getCleanMappings();
		Integer sum2 = mappingsLogMapBioLLM.size();
		System.out.println("Clean mappings after second repair: " + sum2);
		System.out.println("Total mappings after second repair: " +  sum2);
		
		
		return mappingsLogMapBioLLM;
	}
	
	public void printExperimentList() {
		System.out.println("There are " + this.experimentSet.size() + " experiments in the list:\n");
		Integer countExp = 0;
		for(String expKey:this.experimentSet.keySet()) {
			countExp++;
			System.out.println(countExp.toString() + ": " + expKey);
		}
	}
	
	public void saveOntologyMappings(Set<MappingObjectStr> mappingsLogMapBioLLM, 
			HashMap<String,String> singleExperimentConfig,
			String outputDirPath) {
		System.out.println("End of repair. Total number of LogMapBioLLM mappings: " + mappingsLogMapBioLLM.size());
		String sourceOntoPath = singleExperimentConfig.get("source_onto_path");
		String targetOntoPath = singleExperimentConfig.get("target_onto_path");
		String outputFilename = outputDirPath + "logmapbiollm";
		moUtils.saveOntologyMappings(mappingsLogMapBioLLM,outputFilename, sourceOntoPath, targetOntoPath);
	}
	
	public void printStatsToScreen() {
		//TODO
	}
	
		
	public void printStatsToFile() {
		
	}
	
	public static void main(String[] args) {

		/*
		 * Set-up
		 */
		String experimentPath = "/home/valentina/Data/logmapbiollm_paper/table_2_results/experiment_logmapbiollm_bioml2025.json";
		String parentFolderPath = "/home/valentina/Data/logmapbiollm_paper/table_2_results/mappings/";
		MakeLogMapBioLLMMappings fullExperimentSet = new MakeLogMapBioLLMMappings();
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
			
			for(String config:singleExperimentConfig.keySet()) {
				System.out.println(config + ": " + singleExperimentConfig.get(config));
			}
			//TODO filter list of experiments to only contain what we want (logmapbiollm) when we read the experiment list,not here
			if(singleExperimentConfig.get("experiment_type").equals("logmapbiollm")) {
				String outputDirPath = new File(parentFolderPath, exp).toString();
				Set<MappingObjectStr> mappingsLogMapBioLLM = fullExperimentSet.runLogMapBioLLMExperiment(singleExperimentConfig, parentFolderPath);
				fullExperimentSet.saveOntologyMappings(mappingsLogMapBioLLM, singleExperimentConfig, outputDirPath);
			}
		}
	}
}
		

		
