/*
 * Small class to extract llm-annotated mappings
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.util.Set;

import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;


public class ProcessLLMAnnotatedMappings{
	

public static void main(String[] args) {
	
	//TODO none of this should be hard-coded. Resume config files
	
	/*
	 * Input data
	 */
	String onto1 ="/home/valentina/Data/OAEI-input/oaei-2026-input/bio-ml-ontologies/NCIT-bd7a9a8a75e5-Thesaurus.owl";
	String onto2 = "/home/valentina/Data/OAEI-input/oaei-2026-input/bio-ml-ontologies/DOID-611355c44553-doid.owl";
	String llmAnnotatedInPath = "/home/valentina/Data/composed-llm-annotated/bioml2026-annotated/BioML-qwen-annotated-results/bioml2026-ncit-doid-all-composed-minus-logmapllm-mutualsub.annotated.tsv";
	
	/*
	 * Expected output
	 */
	String basePath = "/home/valentina/Data/OAEI-output/oaei-2026-bio-ml/oaei-2026-ncit-doid-noMONDO/";
	String outPath = basePath + "llm-annotated-composed-mappings/";
	String llmTrueOutPath = outPath + "new-composed-annotated-true";
	
	//TODO write or re-use a utils function to do this
	File outDir = new File(outPath);
	if(!outDir.exists()) outDir.mkdir();

	MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
	
	

	Set<MappingObjectStr> llmAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedInPath,"allMappings");
	Set<MappingObjectStr> llmTrueAnnotatedMappings = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedInPath,"onlyTrue");
	moUtils.saveOntologyMappings(llmTrueAnnotatedMappings, llmTrueOutPath, onto1, onto2);
	
	System.out.println("There are " + llmAnnotatedMappings.size() + " mappings in llm-oracle set, of which " + 
	llmTrueAnnotatedMappings.size() + " are true");

		
		
	}
}