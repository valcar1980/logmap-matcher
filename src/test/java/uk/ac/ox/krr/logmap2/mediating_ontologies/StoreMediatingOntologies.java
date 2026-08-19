package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import uk.ac.ox.krr.logmap2.io. OutPutFilesManager;
import uk.ac.ox.krr.logmap2.bioportal.RESTBioPortalAccess;

public class StoreMediatingOntologies {
	
	// constructor
	public StoreMediatingOntologies() {}
			
			
			
	public void saveOntology(String ontoLabel, OWLOntology inputOntology, String basePath){
	
	
			//Create a file for the new format
			String mo_output_path = basePath+ ontoLabel + ".owl";
			System.out.println("Storing ontology at " + mo_output_path);
			File outFile= new File(mo_output_path);
			OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
			OWLDocumentFormat format = new RDFXMLDocumentFormat();
			IRI outputStream = IRI.create(outFile);
			try {
				onto_manager.saveOntology(inputOntology, format, outputStream);
			} catch (OWLOntologyStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
	public OWLOntology CallBioPortal(String onto_label, String base_output_path) {
		
		RESTBioPortalAccess bioportal = new RESTBioPortalAccess();
		if (bioportal.isActive()) {
		
			System.out.println("BioPortal is active: ");
			
			OWLOntology moDownload = bioportal.downLoadOntology(onto_label, 3);
			System.out.println("Downloaded ontology" + moDownload.getOntologyID());
			
			return moDownload;
		}
		else {
			// TODO return an empty ontology or throw exception
			return null;
		}
	}
	
	public static void main(String[] args) {
		String basePath ="/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/";
		String ontoLabel = "MIO";
		StoreMediatingOntologies moStorer = new StoreMediatingOntologies();
		OWLOntology moDownload = moStorer.CallBioPortal(ontoLabel, basePath);
		moStorer.saveOntology(ontoLabel, moDownload, basePath);
		System.out.println("All Stored");
	}
	
	
	
}