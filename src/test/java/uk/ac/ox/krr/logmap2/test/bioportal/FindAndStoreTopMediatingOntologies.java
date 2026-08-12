
package uk.ac.ox.krr.logmap2.test.bioportal;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
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
	public static void main(String[] args) {
		/*
		 * The list of ontologies will be private List<String>
		 * selectedMediatingOntologies = new ArrayList<String>(); coming from
		 * MediatingOntologyExtractor in the package uk.ac.ok.krr.logmap2.bioportal
		 * 
		 */

		OWLOntology onto1;
		OWLOntology onto2;

		/* input location */
		String onto1_iri = "file:/home/valentina/MyData/onto_bioportal/CVRG_EPOntology.owl";
		String onto2_iri = "file:/home/valentina/MyData/onto_bioportal/MIO.owl";

		/* output location */

		String base_output_path = "/home/valentina/git-repos/test-data/test_onto_output";
		/* Check if folder exists otherwise create? */
		if (Files.isDirectory(Paths.get(base_output_path))) {
			System.out.println("Provided output directory " + base_output_path + " exists.");
		}

		// From UsingLogMapMatcher
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		try {
			// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
			config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			onto_manager.setOntologyLoaderConfiguration(config);
			System.out.println("Loading the first ontology " + onto1_iri);
			onto1 = onto_manager.loadOntology(IRI.create(onto1_iri));
			System.out.println("Loading the second ontology " + onto2_iri);
			onto2 = onto_manager.loadOntology(IRI.create(onto2_iri));

			System.out.println("Starting the matching task using LogMap2_Matcher");
			LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1, onto2);
			// Optionally LogMap also accepts the IRI strings as input
			// LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1_iri, onto2_iri);
			System.out.println("Completing the matching task using LogMap2_Matcher");
			// Set of mappings computed my LogMap
			Set<MappingObjectStr> logmap2_mappings = logmap2.getLogmap2_Mappings();

			System.out.println("Number of mappings computed by LogMap: " + logmap2_mappings.size());

			// Accessing mapping objects

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
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
