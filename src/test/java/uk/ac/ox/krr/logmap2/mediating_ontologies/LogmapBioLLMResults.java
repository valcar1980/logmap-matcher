/*
 * Where we do the work described in https://app.zenhub.com/workspaces/test-vava-6a8853824b10b0001cb595a3/issues/zh/18
 * That is, combine annotated composed mappings (from mediating ontologies) with mappings obtained by LogmapLLM to produce
 * LogmapBioLLM.
 */


package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.krr.logmap2.io.ReadFile;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

//import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;

public class LogmapBioLLMResults{
	
	public void setExperimentDetails(){
		
		// Loop through OAEI 2025
		// Loop through OAEI 2026
	}
	
	/**
	 * Load mappings from annotated TSV file
	 * Each row has tab-separated elements and it is expected to have the following structure:
	 * Source,Target,Prediction,Confidence
	 * For example:
	 * http://human.owl #NCI_C49191 	http://mouse.owl#MA_0000702		=	0.47 	CLS		False
	 * @param fullPath
	 */
	public static Set<MappingObjectStr> readAnnotatedMappingsFromTSV(String fullPath) {
		
		
		Set<MappingObjectStr> mapSet = new HashSet<MappingObjectStr>();
		try {
			
			File tsv = new File(fullPath);
			ReadFile reader = new ReadFile(tsv);
				
			
			
			
			int countTrue = 0;
			int countFalse = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				String[] lineElements;
				System.out.println(line);
				if (line.startsWith("#") || !line.startsWith("http")){ //skip comments and header row
					//line=reader.readLine();
					continue;
				}
				
				if (line.indexOf("\t")<0){
					//line=reader.readLine();
					continue;
				}
				
				lineElements=line.split("\t");
					
				System.out.println(lineElements[0] + "  " + lineElements[1]  + "  " + lineElements[5]);
				
				if (Boolean.parseBoolean(lineElements[5].toLowerCase())) {
					System.out.println("Found some truth!");
					//TODO it might not be equivalence, I need to check elements[2]
					//TODO it might not be a CLS equivalence, I need to check and remove 0
					MappingObjectStr formattedMap = new MappingObjectStr(lineElements[0],
							lineElements[1], Double.valueOf(lineElements[3]), MappingObjectStr.EQ,0);
				
					mapSet.add(formattedMap);

											
					countTrue++;
				}
				else {
					countFalse++;
				}
				//line=reader.readLine();

			}
			
			reader.closeBuffer();
			System.out.println("Num mapping in oracle: " + countTrue);
			System.out.println("Num mapping NOT in oracle: " + countFalse);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return mapSet;
		
	}
	
	public static void main(String[] args) {
		
		// Load mappings
		
		String composedMappingsFile = "";
		String llmDefaultMappingsFile = "/home/valentina/Data/anatomy-composed-llm-annotated/all-composed-minus-llm-default.annotated.tsv";
		String llmMutualSubMappingsFile = "/home/valentina/Data/anatomy-composed-llm-annotated/all-composed-minus-llm-mutualsub.annotated.tsv";
		
		Set<MappingObjectStr> llmDefaultMaps = readAnnotatedMappingsFromTSV(llmDefaultMappingsFile);
		
		
		
		ProcessComposedMappings moProcess = new ProcessComposedMappings(); // saveToCSV = true;

		
		
		// Load The logmap-bio results (composed - logmap)
		//String LogmapBioMapsFile = ""'
		//FlatAlignmentReader mappingReader = new FlatAlignmentReader(LogmapBioMapsFile);

		//LogmapBioMaps = ';
		
		//Load the logmap-llm results default
		//Load the logmap-llm results mutual subsumption
		
		// Produce the logmap-bio-llm results default
		
		// The logmap-bio-llm results with mutual subsumption
		
	}
}