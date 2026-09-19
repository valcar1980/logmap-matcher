/*
 * Where we do the work described in https://app.zenhub.com/workspaces/test-vava-6a8853824b10b0001cb595a3/issues/zh/18
 * That is, combine annotated composed mappings (from mediating ontologies) with mappings obtained by LogmapLLM to produce
 * LogmapBioLLM.
 */


package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.krr.logmap2.io.ReadFile;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;

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
				//System.out.println(line);
				if (line.startsWith("#") || !line.startsWith("http")){ //skip comments and header row
					continue;
				}
				
				if (line.indexOf("\t")<0){
					continue;
				}
				
				lineElements=line.split("\t");
					
				//System.out.println(lineElements[0] + "  " + lineElements[1]  + "  " + lineElements[5]);
				
				if (Boolean.parseBoolean(lineElements[5].toLowerCase())) {
					//System.out.println("Found some truth!");
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
	
	public static double[] computePrecisionAndRecall(Set<MappingObjectStr> referenceMappings, Set<MappingObjectStr> testMappings ) {
		Set<MappingObjectStr> intersection = new HashSet<MappingObjectStr>(referenceMappings);
		intersection.retainAll(testMappings);
		System.out.println("TP:" + intersection.size() + "\t TP + FP: " + testMappings.size() +
		"\tTP + FN:" + referenceMappings.size());
		
		double precision;
		double recall;
		
		precision = ((double) intersection.size())/ ((double) testMappings.size());
		System.out.println(precision);

		recall = ((double) intersection.size()) /((double) referenceMappings.size());
		System.out.println(recall);

		
		double[] stats = new double[2];
		stats[0] = (double)Math.round(precision*1000d)/1000d;
		stats[1] = (double)Math.round(recall*1000d)/1000d;
		
		return stats;
		
	}
	
	public static double computeF1score(double precision, double recall) {
		
		double f1score = 2* (precision*recall)/(precision + recall);
		f1score = (double)Math.round(f1score*1000d)/1000d;
		return f1score;
	}
	
	public static void main(String[] args) {
		//Load the OAEI reference
		String raMapsFile = "/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/reference.rdf";
		MappingsReaderManager raMapsReader = new MappingsReaderManager(raMapsFile, "RDF");
		Set<MappingObjectStr> raMappings = Collections.emptySet();

		try {
		raMappings = raMapsReader.getMappingObjects();
		System.out.println("Reference set of mappings contains " + raMappings.size() + " mappings");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		// onto1
		String onto1_iri = "/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/mouse.owl";
		//onto2
		String onto2_iri = "/home/valentina/Data/OAEI-input/oaei-2025-input/anatomy-dataset/human.owl";
		MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		String parentPath = "/home/valentina/Data/OAEI-output/oaei-2025-anatomy/oaei-human-mouse/logmapBioLLM/";
		// Load the mappings found by Logmap LLM (default and mutual subsumption)
		String llmDefaultMapsFile = "/home/valentina/Data/OAEI-input/logmap-llm/anatomy/logmap-llm-default/mouse-human.rdf";
		String llmMutualSubMapsFile = "/home/valentina/Data/OAEI-input/logmap-llm/anatomy/logmap-llm-mutual-subsumption/mouse-human.rdf";
		MappingsReaderManager llmDefaultMappingReader = new MappingsReaderManager(llmDefaultMapsFile, "RDF");
		MappingsReaderManager llmMutualSubMappingReader = new MappingsReaderManager(llmMutualSubMapsFile, "RDF");
		
		
		// Load  composed mappings after they have been annotated using LLM (default and mutual subsumption)
		
		String composedAnnotatedWithLLMDefaultPath = "/home/valentina/Data/anatomy-composed-llm-annotated/all-composed-minus-llm-default.annotated.tsv";
		String composedAnnotatedWithLLMMutualSubPath= "/home/valentina/Data/anatomy-composed-llm-annotated/all-composed-minus-llm-mutualsub.annotated.tsv";
		
		// Read only the mappings that were annotated as True
		
		//LLM Default
		Set<MappingObjectStr> llmTrueComposedMapsWithLLMDefault = readAnnotatedMappingsFromTSV(composedAnnotatedWithLLMDefaultPath);
		System.out.println("Composed mappings that are true according to LLM(Default): " + llmTrueComposedMapsWithLLMDefault.size() + " mappings");
		Set<MappingObjectStr> LogmapLLMBio_default = new HashSet<>();
		LogmapLLMBio_default.addAll(llmTrueComposedMapsWithLLMDefault); 
		
		//LLM Mutual Subsumption
		Set<MappingObjectStr> llmTrueComposedMapsWithLLMMSub = readAnnotatedMappingsFromTSV(composedAnnotatedWithLLMMutualSubPath);
		System.out.println("Composed mappings that are true according to LLM(mutual subsumption): " + llmTrueComposedMapsWithLLMMSub.size() + " mappings");
		Set<MappingObjectStr> LogmapLLMBio_msub = new HashSet<>();
		LogmapLLMBio_msub.addAll(llmTrueComposedMapsWithLLMMSub); 

		// Add the mappings obtained by LogmapLLM
		
		//Get mappings from LogmapLLM Default
		Set<MappingObjectStr> llmDefaultMappings = Collections.emptySet();
		Set<MappingObjectStr> llmMutualSubMappings = Collections.emptySet();

		try {
		llmDefaultMappings = llmDefaultMappingReader.getMappingObjects();
		//System.out.println("Logmap LLM (default) set of mappings contains " + llmDefaultMappings.size() + " mappings");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\n**** LogmapLLM ****\n\n");

		// Precision, Recall, F1
		double[] statsLLMDefault = computePrecisionAndRecall(raMappings,llmDefaultMappings);
		double f1LLMDefault = computeF1score(statsLLMDefault[0],statsLLMDefault[1]);
		System.out.println("Logmap LLM(Default) precision: " + Double.toString(statsLLMDefault[0]) + 
				" \t recall:" + Double.toString(statsLLMDefault[1])+ "\t F1:"+ Double.toString(f1LLMDefault) +"\n\n");
		
		
		LogmapLLMBio_default.addAll(llmDefaultMappings);
		//System.out.println("LogmapBioLLM(Default) contains: " + LogmapLLMBio_default.size() + " mappings");
		
		// Save the mappings
		String LogmapLLMBio_defaultName = parentPath + "logmapBioLLMDefaultResults";
		moUtils.saveOntologyMappings(LogmapLLMBio_default, LogmapLLMBio_defaultName, onto1_iri, onto2_iri);
		
		//Get mappings from LogmapLLM mutual subsumption
		
		try {
		llmMutualSubMappings = llmMutualSubMappingReader.getMappingObjects();
		System.out.println("Logmap LLM (mutual subsumption) set of mappings contains " + llmMutualSubMappings.size() + " mappings");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("LogmapBioLLM(Mutual Subsumtpion) contains: " + llmMutualSubMappings.size() + " mappings");

		
		// Precision, Recall, F1
		double[] statsLLMMSub = computePrecisionAndRecall(raMappings,llmMutualSubMappings);
		double f1LLMMSub = computeF1score(statsLLMMSub[0],statsLLMMSub[1]);
		System.out.println("Logmap LLM(Mutual Subsumption) precision: " + Double.toString(statsLLMMSub[0]) + 
				" \t recall:" + Double.toString(statsLLMMSub[1]) + "\t F1:" + Double.toString(f1LLMMSub) + "\n\n");
		
		
		
		LogmapLLMBio_msub.addAll(llmMutualSubMappings);
		// Save the mappings
		String LogmapLLMBio_msubName = parentPath + "logmapBioLLMMSubResults";
		moUtils.saveOntologyMappings(LogmapLLMBio_msub, LogmapLLMBio_msubName, onto1_iri, onto2_iri);
		
		System.out.println("\n\n**** LogmapBioLLM ****\n\n");
		
		// Precision, Recall, F1
		double[] statsDefault = computePrecisionAndRecall(raMappings, LogmapLLMBio_default);
		double f1Default = computeF1score(statsDefault[0], statsDefault[1]);
		System.out.println("LogmapBioLLM(Default) precision: " + Double.toString(statsDefault[0]) + " \t recall:" + statsDefault[1]+ 
				"\tF1:" + f1Default + "\n\n");


		double[] statsMSub = computePrecisionAndRecall(raMappings, LogmapLLMBio_msub);
		double f1MSub = computeF1score(statsMSub[0], statsMSub[1]);
		System.out.println("LogmapBioLLM(MutualSubsumption) precision: " + Double.toString(statsMSub[0]) + " \t recall:" + statsMSub[1]+ 
				"\tF1:" + f1MSub + "\n\n");

	}
}