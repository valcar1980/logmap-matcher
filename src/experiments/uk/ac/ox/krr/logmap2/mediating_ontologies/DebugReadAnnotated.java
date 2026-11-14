package uk.ac.ox.krr.logmap2.mediating_ontologies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import uk.ac.ox.krr.logmap2.io.ReadFile;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class DebugReadAnnotated {
	
	/**
	 * Reads mappings that have been annotated using logmap-LLM, therefore it provides count of how
	 * many mappings were approved by the oracle and how many were rejected
	 * Each row has tab-separated elements and is expected to have the following structure:
	 * Option A (Short)
	 * Source,Target,Prediction,Confidence For example: http://human.owl #NCI_C49191
	 * http://mouse.owl#MA_0000702 = 0.47 CLS False
	 * Option B (Long)
	 * 
	 * @param fullPath
	 * @param readOption String There are two options "onlyTrue" (extracts only mappings labelled as true) and "allMappings"
	 * @return
	 */
	public Set<MappingObjectStr> readAnnotatedMappingsFromTSV(String fullPath, String readOption) {
		
		if(!(readOption.equals("onlyTrue") || readOption.equals("allMappings"))) {
			System.out.println("Wrong TSV reading option provided, check for typos");
			return null;
		}
		
		Set<MappingObjectStr> mapSet = new HashSet<MappingObjectStr>();
		try {

			File tsv = new File(fullPath);
			ReadFile reader = new ReadFile(tsv);

			int countTrue = 0;
			int countFalse = 0;
			int lineCount =0;
			int countSkipped=0;
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				lineCount++;
				System.out.println("line number :\t" + lineCount);
				String[] lineElements;
				// System.out.println(line);
				if (line.startsWith("#") || !line.startsWith("http")) { // skip comments and header row
					System.out.println("Skipping line :\t" + line);
					//countSkipped++;
					continue;
				}

				if (line.indexOf("\t") < 0) {
					System.out.println("Skipping line :\t" + line);
					//countSkipped++;

					continue;
				}
				
				
				lineElements = line.split("\t");
				System.out.println("Line length: " + lineElements.length);
				System.out.println("Line broken down into: ");
				for(String ele: lineElements) {
					System.out.print(ele + "\t");
				}
				System.out.print("\n");
				
				
				
				if (lineElements.length<9) {

					System.out.println("Skipping line  as too short:\t" + line);
					countSkipped++;
					try {
					TimeUnit.SECONDS.sleep(1);}catch(Exception e) {}
					continue;
				}
				
				/*
				 * Two options, retrieve only true mappings or retrieve all of them
				 */
				
				
				
				
				if (readOption.equals("onlyTrue")) {

					// if the value at position 5 is true, add to mappings
					
					if (Boolean.parseBoolean(lineElements[5].toLowerCase())) {
						// System.out.println("Found some truth!");
						// TODO it might not be equivalence, I need to check elements[2]
						// TODO it might not be a CLS equivalence, I need to check and remove 0
						MappingObjectStr formattedMap = new MappingObjectStr(lineElements[0], lineElements[1],
								Double.valueOf(lineElements[3]), MappingObjectStr.EQ, 0);

						mapSet.add(formattedMap);

						countTrue++;
					} else {// skip mapping
						countFalse++;
					}
				}
				
				if(readOption.equals("allMappings")) {
					MappingObjectStr formattedMap = new MappingObjectStr(lineElements[0], lineElements[1],
							Double.valueOf(lineElements[3]), MappingObjectStr.EQ, 0);
					mapSet.add(formattedMap);
					if (Boolean.parseBoolean(lineElements[5].toLowerCase())) countTrue++;
					else countFalse++;
					
					
				}
			}

			reader.closeBuffer();
			
			System.out.println("We skipped " + countSkipped + "lines because too short, out of " + lineCount);
			System.out.println("Num mapping in oracle: " + countTrue);
			System.out.println("Num mapping NOT in oracle: " + countFalse);
		
		}catch (FileNotFoundException e) {
			System.out.println("Error reading file " + fullPath + " mapping set to null");
			mapSet = null;
			e.printStackTrace();
		}

		return mapSet;

	}

	/*
	 * The problem was simply that I was using the wrong input, mappings for annotation rather than those annotated.
	 * TODO make a function (or extend existing) to read mappings for annotation files, and/or flag them in the read function
	 */
	
	public static void main(String[] args) {
		
		//String omim_ordo = "/home/valentina/Data/OAEI-output/OAEI_results_bioml2025_largebio2021/bioml2025/oaei-omim-ordo-noMONDO/composed-mappings-for-llm-annotation/all-composed-minus-llm-mutualsub-20261007_160638.tsv";
		String omim_ordo = "/home/valentina/Data/composed-llm-annotated/logmap-composed-mappings-bioml-2025_ANNOTATED/omim-ordo-noMONDO/all-composed-minus-llm-mutualsub-20261007_160638.annotated.tsv";
		String ncit_doid = "/home/valentina/Data/composed-llm-annotated/logmap-composed-mappings-bioml-2025_ANNOTATED/ncit-doid-noMONDO/all-composed-minus-llm-mutualsub-20261007_153028.annotated.tsv";
		String test_omim_ordo = "/home/valentina/Data/logmapbiollm_paper/table_2_results/test_omim_ordo.tsv";
		String test_ncit_doid = "/home/valentina/Data/logmapbiollm_paper/table_2_results/test_ncit_doid.tsv";
		String llmAnnotatedMappingsPath = "/home/valentina/Data/composed-llm-annotated/logmap-composed-mappings-largebio-2021_ANNOTATED/snomed-nci/all-composed-minus-llm-mutualsub.annotated.tsv";

		//MediatingOntologiesUtils moUtils = new MediatingOntologiesUtils();
		DebugReadAnnotated moUtils = new DebugReadAnnotated();
//		System.out.println("snomed-nci full");
//		try {
//		TimeUnit.SECONDS.sleep(1);
//		}catch(Exception e) {}
//		Set<MappingObjectStr> t1 = moUtils.readAnnotatedMappingsFromTSV(llmAnnotatedMappingsPath,  "onlyTrue");
//		System.out.println("ncit-doid test");
//		try {
//		TimeUnit.SECONDS.sleep(1);
//		}catch(Exception e) {}
//		Set<MappingObjectStr> nd = moUtils.readAnnotatedMappingsFromTSV(test_ncit_doid, "onlyTrue");
//		System.out.println("omim-ordo test");
//		try {
//		TimeUnit.SECONDS.sleep(1);
//		}catch(Exception e) {}
		Set<MappingObjectStr> oo = moUtils.readAnnotatedMappingsFromTSV(test_omim_ordo, "onlyTrue");
		System.out.println("omim-ordo full");
		try {
		TimeUnit.SECONDS.sleep(1);
		}catch(Exception e) {}
		Set<MappingObjectStr> Bigoo = moUtils.readAnnotatedMappingsFromTSV(omim_ordo, "onlyTrue");

		
		
		

	}
}