package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class MakeLogMapBioMappings implements ExperimentInterface {
	public String parentFolderPath;
	public OWLOntology sourceOnto;
	public OWLOntology targetOnto;
	public Set<MappingObjectStr> mappingLogMap;
	public Set<MappingObjectStr> uniqueMOMappings;
	MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
	
	
	public MakeLogMapBioMappings() {
	}
	


	@Override
	public void readExperimentConfigs(String experimentPath) {
		File jsonConfig = new File(experimentPath);
		try {
		if (jsonConfig.exists()==false) {
			System.out.println("Couldn't find config JSON in parent folder " + experimentPath);
			return;
		} else {
			
			System.out.println("Found config JSON in parent folder " + experimentPath);
			ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode rootNode = objectMapper.readTree(jsonConfig);
	 
	        System.out.println(rootNode.size());
	        List<String> keysList = new ArrayList<>();
	        if (rootNode.isObject()) {
	            Iterator<String> fieldNames = rootNode.fieldNames();
	            while (fieldNames.hasNext()) {
	            	String experimentName = fieldNames.next();
	            	System.out.println(experimentName);
	                keysList.add(experimentName);
	            }
	            }
	            
	         for( String exp: keysList) {
	        	 JsonNode innerNode = rootNode.get(exp);
	         
	        	 if (innerNode != null && innerNode.isObject()) {
	        	        
	        	        // Extract the values as Java Strings using asText()
	        	        String sourcePath   = innerNode.path("source_onto_path").asText();
	        	        String targetPath   = innerNode.path("target_onto_path").asText();
	        	        String logmapPath   = innerNode.path("logmap_mappings_path").asText();
	        	        String uniqueMoPath = innerNode.path("unique_mo_mappings_path").asText();
	        	        
	        	        // Print out the extracted values
	        	        System.out.println("Source Onto Path:    " + sourcePath);
	        	        System.out.println("Target Onto Path:    " + targetPath);
	        	        System.out.println("LogMap Mappings:     " + logmapPath);
	        	        System.out.println("Unique MO Mappings:  " + uniqueMoPath);
	        	    } else {
	        	        System.out.println("This inner object is empty or missing.");
	        	 
	         }
	        }
	        }
	        }catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void loadExperimentInput() {
		// TODO Auto-generated method stub
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		onto_manager.setOntologyLoaderConfiguration(config);
		

		// source: onto1
		String onto1_iri = moUtils.sourceOntoPath;

		sourceOnto = null;

		try {
			System.out.println("Loading source ontology " + onto1_iri);
			sourceOnto = onto_manager.loadOntology(IRI.create("file:" + onto1_iri));
		} catch (Exception e) {
			System.out.println("Failed to load source ontology" + onto1_iri);
		}

		// target: onto2
		String onto2_iri = moUtils.targetOntoPath;
		targetOnto = null;

		try {
			System.out.println("Loading target ontology " + onto2_iri);
			targetOnto = onto_manager.loadOntology(IRI.create("file:" + onto2_iri));

		} catch (Exception e) {
			System.out.println("Failed to load target ontology" + onto2_iri);
		}
		
		String mappingLogMapPath = moUtils.source2targetLogmapMappingsPath;
		mappingLogMap = null;
		
		try {
			System.out.println("Loading LogMap mappings from " + mappingLogMapPath);
			mappingLogMap = moUtils.readMappingsFromFile(mappingLogMapPath);
		}
		catch(Exception e) {
			System.out.println("Failed to load LogMap mappings from " + mappingLogMapPath);
			
		}
		
		String uniqueMOMappingsPath = moUtils.newUniqueMappingsPath;
		uniqueMOMappings = null;
		
		try {
			System.out.println("Loading Mediating Ontologies unique composed mappings from " + uniqueMOMappingsPath);
			mappingLogMap = moUtils.readMappingsFromFile(mappingLogMapPath);
		}
		catch(Exception e) {
			System.out.println("Failed to load Mediating Ontologies unique composed mappings from " + uniqueMOMappingsPath);
			
		}
		
	}

	@Override
	public void runExperimentPipeline() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeExperimentOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveExperimentDataLineageToFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printExperimentStatsToScreen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveExperimentLogsToFile() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		String experimentPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/experiment_logmapbio_mappings.json";
		String parentFolderPath = "/home/valentina/Data/logmapbiollm-paper/CTLM28_mappings_for_OAEI_evaluator/mappings/";
		MakeLogMapBioMappings lmapbio = new MakeLogMapBioMappings();
		lmapbio.readExperimentConfigs(experimentPath);
		
		
		
	}
}