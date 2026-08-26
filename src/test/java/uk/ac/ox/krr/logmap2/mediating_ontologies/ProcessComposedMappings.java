package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;

public class ProcessComposedMappings{
	/**
	 * Performs set subtraction between sets of mappings. Works for any two sets of mappings. However,
	 * in this class, we care about removing the mappings originally provided by logmap
	 * from the set of mappings obtained via the mediating ontology.
	 * @param mediatingComposedMappings
	 * @param logmapMappings
	 * @return mapSubtracted;
	 */
	public static Set<MappingObjectStr> mappingSetSubtraction(Set<MappingObjectStr> mediatingComposedMappings, Set<MappingObjectStr> logmapMappings){
		Set<MappingObjectStr> mapSubtracted = new HashSet<MappingObjectStr>();
		
		for(MappingObjectStr mapping: mediatingComposedMappings) {
			
			if(logmapMappings.contains(mapping)==false) {
				mapSubtracted.add(mapping);
			}
		}
		
		return mapSubtracted;
	}
	public static void main(String[] args) {
		//1. read from config file the parentPath
		//2. check that there is a folder with composed mappings
		// Load source2target mappings as logmap original mappings
		String sourceTargetMappingsFile ="/home/valentina/git-repos/test-data/test-argparse/store-source-target/source2target.txt";
		try {
			FlatAlignmentReader mappingReader = new FlatAlignmentReader(sourceTargetMappingsFile);
			Set<MappingObjectStr> mapSource2Target = mappingReader.getMappingObjects();
			System.out.println("Original set of mappings contains " + mapSource2Target.size() + " mappings");
			//3. load one mapping file
			String moMappingsPath = "/home/valentina/git-repos/test-data/test_onto_output/test-mediating-store/test-mappings/";
			String moComposedMappings = moMappingsPath + "OBA.txt";
			FlatAlignmentReader mappingReader_i = new FlatAlignmentReader(moComposedMappings);
			Set<MappingObjectStr> composedMappings_i = mappingReader_i.getMappingObjects();
			System.out.println("Mediating ontology gave a total of " + composedMappings_i.size() + " mappings");
			Set<MappingObjectStr> newMappings = mappingSetSubtraction(composedMappings_i, mapSource2Target);
			System.out.println("Of which new mappings are " + composedMappings_i.size());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//4. subtract the mappings that were already found by Logmap (source,target)

	}
	
	
}