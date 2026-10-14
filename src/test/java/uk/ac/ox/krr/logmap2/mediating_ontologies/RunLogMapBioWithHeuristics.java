/*
 * Run LogMapBio with heuristics for ontology alignment
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.LogMap2_OAEI_BioPortal;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class RunLogMapBioWithHeuristics{
	
	public static void main (String[] args) {
		
		LogMap2_OAEI_BioPortal logmap_Bio = new LogMap2_OAEI_BioPortal();
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		
		// Take onto1 and onto2
		String onto1Path = "file:/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/mouse.owl";
		String onto2Path = "file:/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/human.owl";
		String iri1 = onto1Path;
		String iri2 = onto2Path;
		
		// Select composed mappings (or any mediating mapping...)
		String mappingPath = "/home/valentina/Data/OAEI-output/oaei-2025-anatomy/oaei-human-mouse/composed-mappings-for-llm-annotation/all-composed.txt";
		Set<MappingObjectStr> newComposedMappings = moUtils.readMappingsFromFile(mappingPath);
		
		
		//Upload logmapBio as already computed
		
		String oldMappingPath = "/home/valentina/Data/tests/oaei-human-mouse/logmapbioheu/logmapbiomappings.txt";
		Set<MappingObjectStr> oldMappings = moUtils.readMappingsFromFile(oldMappingPath);
		
		
		ProcessComposedMappings cheekySwapper = new ProcessComposedMappings();
		Boolean sameMappingOrder= cheekySwapper.compareMappingOrder(newComposedMappings, oldMappings);
		if (sameMappingOrder==false) {
			newComposedMappings = cheekySwapper.swapMappingOrder(newComposedMappings);
			System.out.println("Had to reverse mapping directions");
		}
		
		String fileOut = "/home/valentina/Data/tests/oaei-human-mouse/logmapbioheu/logmapbiomappings";

//		//Run LogMapBio from scratch
//		try {
//			logmap_Bio.align(iri1, iri2); // this gets also the 10 mediating ontologies :O
//			//logmap_Bio.returnAlignmentFile(file_out);
//			
//			Set<MappingObjectStr> logmapBioMappings = logmap_Bio.getLogMapBio_Mappings();
//			moUtils.saveOntologyMappings(logmapBioMappings, fileOut,iri1, iri2 ); 			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// TODO:Run matcher using same new mappings as different experiment
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		onto_manager.setOntologyLoaderConfiguration(config);
		try {
		OWLOntology onto1 = onto_manager.loadOntology(IRI.create(iri1));
		OWLOntology onto2 = onto_manager.loadOntology(IRI.create(iri2));
		
		LogMap2_Matcher logmapBioMappingsV2 = new LogMap2_Matcher(onto1,onto2, newComposedMappings);
		
		String fileOutV2 = fileOut + "V2";
		moUtils.saveOntologyMappings(logmapBioMappingsV2.getLogmap2_Mappings(), fileOutV2, iri1, iri2);
	}catch(Exception e){
		e.printStackTrace();
	}
	}
	
}