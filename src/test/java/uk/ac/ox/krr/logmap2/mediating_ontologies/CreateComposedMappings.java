package uk.ac.ox.krr.logmap2.mediating_ontologies;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
// import uk.ac.ox.krr.logmap2.io.OutPutFilesManager;
// import uk.ac.ox.krr.logmap2.LogMap2_OAEI_BioPortal;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class CreateComposedMappings{
	
	
	
	public static void main(String[] args) {
		
		
		String onto1_iri = "file:/home/valentina/Downloads/anatomy-dataset/human.owl";
		String onto2_iri = "file:/home/valentina/Downloads/anatomy-dataset/mouse.owl";

		// String parentPath = "/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/";
		// String filePath = parentPath + "logmap_top10_mediating_ontologies.txt";
		/*
		 * 1. Get  the two original ontologies and at least one mediating ontology
		 */
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();

		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();

		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		onto_manager.setOntologyLoaderConfiguration(config);
		
		try {
			System.out.println("Loading the first ontology " + onto1_iri);
			OWLOntology onto1 = onto_manager.loadOntology(IRI.create(onto1_iri));
			System.out.println("Loading the second ontology " + onto2_iri);
			OWLOntology onto2 = onto_manager.loadOntology(IRI.create(onto2_iri));
			String mo_i_iri = "file:" + "/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/MIO.owl";
			System.out.println("Loading the mediating ontology " + mo_i_iri);
			OWLOntology mo_i = onto_manager.loadOntology(IRI.create(mo_i_iri));
			System.out.println("Starting the matching task (onto1, mo_i)");
			LogMap2_Matcher logmap2_onto1_mo_i = new LogMap2_Matcher(onto1, mo_i);
			System.out.println("Completing the matching task (onto1, mo_i) " +logmap2_onto1_mo_i);
			Set<MappingObjectStr>  mo2onto1_maps = logmap2_onto1_mo_i.getLogmap2_Mappings();
			System.out.println("Completing the matching task ( mo_i, onto2). Mappings count " + mo2onto1_maps.size());
			
			System.out.println("Starting the matching task ( mo_i, onto2)");
			LogMap2_Matcher logmap2_mo_i_onto2 = new LogMap2_Matcher(mo_i, onto2);
			Set<MappingObjectStr>  mo2onto2_maps = logmap2_mo_i_onto2.getLogmap2_Mappings();
			System.out.println("Completing the matching task ( mo_i, onto2). Mappings count " + mo2onto2_maps.size());
			
			
			Set<MappingObjectStr>   composed_mappings = new HashSet<MappingObjectStr>();
			
			for (MappingObjectStr map_mo1 : mo2onto1_maps){
				if (!map_mo1.isClassMapping())
					continue;
				for (MappingObjectStr map_mo2 : mo2onto2_maps){
					
					if (!map_mo2.isClassMapping())
						continue;
					
					if (map_mo1.getIRIStrEnt2().equals(map_mo2.getIRIStrEnt1())){
			
						
						MappingObjectStr mapping = new MappingObjectStr(
								map_mo1.getIRIStrEnt1(), 
								map_mo2.getIRIStrEnt2(), 
								(map_mo1.getConfidence()+map_mo2.getConfidence())/2.0, MappingObjectStr.EQ, MappingObjectStr.CLASSES);
						
						composed_mappings.add(mapping);
					}
					}					
			} 
			
			System.out.println("There are " + composed_mappings.size() + " composed mappings");
			//	for (MappingObjectStr cmap:composed_mappings) {
			//	
			//}
			
		}catch( Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("All complete");
		/*
		 * 2. Run logmap to get mappings
		 * 3. Compose mappings
		 * 4. Store mappings
		 * 
		 * 
		 */
		
		
		
		
		
	}
	
	
}