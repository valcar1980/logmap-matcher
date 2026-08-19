package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.ox.krr.logmap2.bioportal.RESTBioPortalAccess;

public class StoreMediatingOntologies {
	
	public StoreMediatingOntologies(){}
	
	public void CallBioPortal(String onto_label, String base_output_path) {
		
		RESTBioPortalAccess bioportal = new RESTBioPortalAccess();
		if (bioportal.isActive()) {
		
			System.out.println("BioPortal is active: ");
			
			OWLOntology MO_download = bioportal.downLoadOntology(onto_label, 3);
			System.out.println("Downloaded ontology" + MO_download.getOntologyID());
			System.out.println("Number of axioms: " + MO_download.getAxiomCount());
			
			//Create a file for the new format
			
			String mo_output_path = base_output_path + onto_label + ".owl";
			System.out.println("Storing ontology at " + mo_output_path);
			File outFile= new File(mo_output_path);
			OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
			OWLDocumentFormat format = new RDFXMLDocumentFormat();
			IRI outputStream = IRI.create(outFile);
			try {
				onto_manager.saveOntology(MO_download, format, outputStream);
			} catch (OWLOntologyStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		String base_output_path ="/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store";
		String onto_label = "MIO";
		StoreMediatingOntologies mo_storer = new StoreMediatingOntologies();
		mo_storer.CallBioPortal(onto_label, base_output_path);
		System.out.println("All Stored");
	}
	
	
	
}