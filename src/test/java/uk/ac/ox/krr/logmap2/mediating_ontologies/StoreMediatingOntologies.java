package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			
	public List<String> getOntologyListFromFile(String filePath) throws IOException {
		
		// list that holds strings of a file
        List<String>  listOfStrings = new ArrayList<String>();
      
        // load data from file
        BufferedReader bf = new BufferedReader(new FileReader(filePath));
      
        // read entire line as string
        String line = bf.readLine();
      
        // checking for end of file
        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }
      
        // closing bufferreader object
        bf.close();
      		
		return listOfStrings;
	}	
			
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
//		String basePath ="/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/";
//		String ontoLabel = "MIO";
			StoreMediatingOntologies moStorer = new StoreMediatingOntologies();
//		OWLOntology moDownload = moStorer.CallBioPortal(ontoLabel, basePath);
//		moStorer.saveOntology(ontoLabel, moDownload, basePath);
		
		String listFile = "/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/logmap_top10_mediating_ontologies.txt";
		try {
			List<String> moList = moStorer.getOntologyListFromFile(listFile);
			for (String ontoStr: moList) {
				System.out.println(ontoStr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("All Stored");
	}
	
	
	
}