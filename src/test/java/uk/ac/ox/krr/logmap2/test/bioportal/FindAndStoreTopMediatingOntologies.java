
package uk.ac.ox.krr.logmap2.test.bioportal;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.bioportal.MediatingOntologyExtractor;
import uk.ac.ox.krr.logmap2.bioportal.RESTBioPortalAccess;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class FindAndStoreTopMediatingOntologies {

	/*
	 * author:valcar1980
	 * 
	 * Algorithm idea:
	 * 
	 * 1. Get two ontologies and pre-computed mappings 2. Run MediateOntologies to
	 * produce an ordered list of top-10 3. Create or provide path to directory for
	 * storing mediating ontologies 4. (optional) perform checks on the existence of
	 * such ontologies 5. Use the urls to fetch those ontologies 6. Store each
	 * ontology in the folder with information about their ranking in the top-10 /
	 * top-15
	 * 
	 * 
	 */

	public LogMap2_Matcher createMappings(String onto1_iri, String onto2_iri) {

		try {

			OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();

			// In case an import is broken
			OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();

			// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
			config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			onto_manager.setOntologyLoaderConfiguration(config);
			System.out.println("Loading the first ontology " + onto1_iri);
			OWLOntology onto1 = onto_manager.loadOntology(IRI.create(onto1_iri));
			System.out.println("Loading the second ontology " + onto2_iri);
			OWLOntology onto2 = onto_manager.loadOntology(IRI.create(onto2_iri));

			System.out.println("Starting the matching task using LogMap2_Matcher");
			LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1, onto2);
			// Optionally LogMap also accepts the IRI strings as input
			// LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1_iri, onto2_iri);
			System.out.println("Completing the matching task using LogMap2_Matcher");
			/*
			 * // Set of mappings computed my LogMap Set<MappingObjectStr> logmap2_mappings
			 * = logmap2.getLogmap2_Mappings(); // Get representative labels Set<String>
			 * logmap2_representative_labels = logmap2.getRepresentativeLabelsForMappings();
			 * System.out.println("Number of mappings computed by LogMap: " +
			 * logmap2_mappings.size()); return logmap2_mappings;
			 */
			return logmap2;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

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
	
	public static void printMediatingOntologies(List<String>  selectedMediatingOntologies) {

		if (selectedMediatingOntologies.size()<1 ){
			System.out.println("No mediating ontologies found");
			
		}
		else {
			for (int i=0; i<selectedMediatingOntologies.size(); i++){
				System.out.println(selectedMediatingOntologies.get(i));
			}
		}
		
	}

	public static void main(String[] args) {
		/*
		 * The list of ontologies will be private List<String>
		 * selectedMediatingOntologies = new ArrayList<String>(); coming from
		 * MediatingOntologyExtractor in the package uk.ac.ok.krr.logmap2.bioportal
		 * 
		 */

		/*
		 * long startTime = System.nanoTime(); //String onto1_iri =
		 * "file:/home/valentina/MyData/onto_bioportal/CVRG_EPOntology.owl"; //String
		 * onto2_iri = "file:/home/valentina/MyData/onto_bioportal/MIO.owl";
		 * 
		 * String onto1_iri =
		 * "file:/home/valentina/Downloads/anatomy-dataset/human.owl"; String onto2_iri
		 * = "file:/home/valentina/Downloads/anatomy-dataset/mouse.owl";
		 * 
		 * FindAndStoreTopMediatingOntologies myLogMap = new
		 * FindAndStoreTopMediatingOntologies();
		 * 
		 * LogMap2_Matcher onto_mappings = myLogMap.createMappings(onto1_iri,onto2_iri);
		 * printOntologyMappings(onto_mappings); Set<String> representative_labels =
		 * onto_mappings.getRepresentativeLabelsForMappings(); long endTime =
		 * System.nanoTime(); System.out.println("Map matching task completed.\t" +
		 * Math.floor((endTime-startTime)/10e9) + " seconds elapsed");
		 * 
		 * startTime = System.nanoTime(); MediatingOntologyExtractor mo_extract = new
		 * MediatingOntologyExtractor(representative_labels); endTime =
		 * System.nanoTime(); System.out.println("Extracted mediating ontologies.\t" +
		 * Math.floor((endTime-startTime)/10e9) + " seconds elapsed"); List<String>
		 * mediating_ontologies = mo_extract.getSelectedMediatingOntologies();
		 * printMediatingOntologies(mediating_ontologies);
		 */
		
		//TODO Save Mediating Ontologies list to file
		
		
		RESTBioPortalAccess bioportal = new RESTBioPortalAccess();
		if (bioportal.isActive()) {
			
			System.out.println("BioPortal is active: ");
			OWLOntology MO_download = bioportal.downLoadOntology("MIO", 3);
			System.out.println("Downloaded ontology" + MO_download.getOntologyID());
			/*
			 * for (int i=0; i<mediating_ontologies.size(); i++) { // Fetch the ontology
			 * from Bioportal OWLOntology MO_download =
			 * bioportal.downLoadOntology(mediating_ontologies.get(i), 3);
			 * System.out.println("Downloaded ontology" + MO_download.getOntologyID()); }
			 */
		}
	}	
}
		
		