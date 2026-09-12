package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;

public class ProcessComposedMappings{
	
	public ProcessComposedMappings(){}
	
	
	/**
	 * Performs set subtraction A\B between sets of mappings. Works for any two sets of mappings A and B. 
	 * @param AMappings
	 * @param BMappings
	 * @return mapSubtracted;
	 */
	public Set<MappingObjectStr> mappingSetSubtraction(Set<MappingObjectStr> AMappings, Set<MappingObjectStr> BMappings){
		Set<MappingObjectStr> mapSubtracted = new HashSet<MappingObjectStr>();
		
		for(MappingObjectStr mapping: AMappings) {
			
			if(BMappings.contains(mapping)==false) {
				mapSubtracted.add(mapping);
			}
		}
		
		return mapSubtracted;
	}
	
	public HashMap<String, FlatAlignmentReader> makeMappingReadersFromDirectory(String mappingsDirectory){
		
		HashMap<String,FlatAlignmentReader> readersArray = new HashMap<String, FlatAlignmentReader>();
		File listPath = new File(mappingsDirectory);
		File[] listOnto = listPath.listFiles();
		String moComposedMappingsPath = null;
		
		for( File f: listOnto) {
			boolean is_txt = f.getName().endsWith(".txt");
			if(is_txt==false) {
				System.out.println("Not a txt " + f);
				continue;
			}else {
				
				System.out.println("Filtering new mappings for " + f.getName());
				moComposedMappingsPath = mappingsDirectory + f.getName();
				FlatAlignmentReader newreader = null;
				String ontoLabel = f.getName().substring(0, f.getName().lastIndexOf('.'));
				try {
					
					newreader = new FlatAlignmentReader(moComposedMappingsPath);
				}
				catch(Exception e){
					System.out.println("Failed to read composed mapping for " + f);
				}
				
				readersArray.put(ontoLabel, newreader);
				

			}
			
		}
		
		
		return readersArray;
	}
	
	public static void main(String[] args) {

		
		ProcessComposedMappings moProcess = new ProcessComposedMappings();
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		moUtils.getParentFolder(args);
		moUtils.readConfigJSON();
		moUtils.createSubDirectoriesFromParent(); //if the subdir already exists it doesn't create it nor overwrite it
		
		String onto1_iri = "file:" + moUtils.sourceOntoPath;
		String onto2_iri = "file:" + moUtils.targetOntoPath;

		String moMappingsPath = moUtils.composedMappingsPath;
		String newMappingsPath = moUtils.newUniqueMappingsPath;
		String sourceTargetMappingsFile =moUtils.sourceToTargetPath + "source2target.txt";
		System.out.println("Location " + sourceTargetMappingsFile);
		try {
			
			FlatAlignmentReader mappingReader = new FlatAlignmentReader(sourceTargetMappingsFile);
			Set<MappingObjectStr> mapSource2Target = mappingReader.getMappingObjects();
			System.out.println("Original set of mappings contains " + mapSource2Target.size() + " mappings");


			HashMap<String,FlatAlignmentReader> readersArray = moProcess.makeMappingReadersFromDirectory(moMappingsPath);
			for( String ontoLabel: readersArray.keySet()) {
				
					Set<MappingObjectStr> moComposedMappings = readersArray.get(ontoLabel).getMappingObjects();
					System.out.println("Mediating ontology gave a total of " + moComposedMappings.size() + " mappings");
					Set<MappingObjectStr> newMappings = moProcess.mappingSetSubtraction(moComposedMappings, mapSource2Target);
					System.out.println("Of which new mappings are " + newMappings.size());
					String newMapPath = newMappingsPath + ontoLabel;
					moUtils.saveOntologyMappings(newMappings, newMapPath, onto1_iri, onto2_iri);

				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
				

		

	