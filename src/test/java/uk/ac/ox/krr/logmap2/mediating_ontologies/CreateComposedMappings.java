package uk.ac.ox.krr.logmap2.mediating_ontologies;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
// import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.io.OutPutFilesManager;
// import uk.ac.ox.krr.logmap2.LogMap2_OAEI_BioPortal;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.FlatAlignmentReader;



/**
 * Takes source and target ontology and their list of mediating ontologies
 * and creates lists of all mappings logmap can recover via these ontologies. Running this code is quite
 * memory intensive, make sure you use the correct system arguments to prevent crash.
 * Program argument  must be full path to the parent folder. 
 */
public class CreateComposedMappings{
	String onto1_iri;
	String onto2_iri;
	String parentPath;
	String basePath;
	String midPath;
	String outPath;
	String listFile; 

	// private final Set<MappingObjectStr>  m2tMaps;

	public CreateComposedMappings() {
	}
	
	public void setOnto1_IRI(String ontoIRI) {
		this.onto1_iri = "file:" + ontoIRI;
	}
	public void setOnto2_IRI(String ontoIRI) {
		this.onto2_iri = "file:" + ontoIRI;
	}
	public void setAllPathsFromParent(String parentPath) {
		this.parentPath = parentPath;
		this.basePath = parentPath + "store-mediating-ontologies/";
		this.midPath = parentPath + "store-simple-mappings/";
		this.outPath = parentPath + "store-composed-mappings/";
		this.listFile = parentPath + "/logmap_top12_mediating_ontologies.txt";
	}
	
	
	public String getOntologyNameFromFile(String pathString){

        Path path = Paths.get(pathString);
        String lastSegment = path.getName(path.getNameCount() - 1).toString();	
        if (lastSegment.endsWith(".owl")) {
        	String ontoName = lastSegment.substring(0, lastSegment.length() - 4);
        	return ontoName;
        }
        else {
        	String ontoName = null;
        	return ontoName;
        }
	}
	
	/**
	 * Takes source, target and one mediating ontology and finds all mappings
	 * for (source, medium) and (medium, target)
	 * @param source OWLOntology the source ontology
	 * @param target OWLOntology the target ontology
	 * @param medium OWLOntology the mediating ontology
	 * @return CreateCompoSedMappings new instance of CreateComposedMappings
	 */
	public String extractAndStoreMappings(OWLOntology source, String sourceName, OWLOntology target, String targetName) {
		
		System.out.println("Starting the matching task (" + sourceName + ", " + targetName + ")");
		LogMap2_Matcher sourceMapTarget= new LogMap2_Matcher(source, target);
		Set<MappingObjectStr>  s2tMaps = sourceMapTarget.getLogmap2_Mappings();
		sourceMapTarget.clearIndexStructures();
		sourceMapTarget = null;
		System.out.println("Completing the matching task. Mappipngs count " +s2tMaps.size());
		String ontoStr = "map_" + sourceName + "_" + targetName;  
		saveComposedMappings(s2tMaps, ontoStr);
		return parentPath+ontoStr;
	}
	
	/**
	 * Reads 2 sets of mappings from file and aggregates them into a single composed mapping file
	 * @param source2mediumPath String	full path to mapping file for source-mediating ontology 
	 * @param medium2targetPath String	full path to mapping file for mediating-target ontology
	 * @return
	 */
	public Set<MappingObjectStr> composeOntologyMappingsFromFile(String source2mediumPath, String medium2targetPath) {
		System.out.println("Reading mappings from file " + source2mediumPath);
		
		try {
		FlatAlignmentReader mappingReader1 = new FlatAlignmentReader(source2mediumPath);
		Set<MappingObjectStr> mapSource2Medium = mappingReader1.getMappingObjects();
		
		System.out.println("Reading mappings from file " + medium2targetPath);
		FlatAlignmentReader mappingReader2 = new FlatAlignmentReader(medium2targetPath);
		Set<MappingObjectStr> mapMedium2Target = mappingReader2.getMappingObjects();
			   
		System.out.println("Aggregating into composed mappings source-target via mediating ontology.");
		Set<MappingObjectStr>   composedMappings =  aggregateComposedMapping(mapSource2Medium, mapMedium2Target);
		// return new CreateComposedMappings(s2mMaps, m2tMaps);
			return composedMappings;
		}
		catch (Exception e){
			//e.printStackTrace();
			System.out.println("Something went wrong, skipping this pair.")	;
			return null;
			}
	}
	
	
	/**
	 * Goes through each mapping in one set and checks if it is also present in the other set. Only then, it is added to the list.
	 * Please note, it only focuses on Class mappings.
	 * @param source2mo Set<MappingObjectStr> mappings between source and mediating ontology
	 * @param mo2target Set<MappingObjectStr> mappings between mediating and target ontology
	 * @return
	 */
	private Set<MappingObjectStr>  aggregateComposedMapping(Set<MappingObjectStr> source2mo, Set<MappingObjectStr> mo2target){
		
		Set<MappingObjectStr>   composedMappings = new HashSet<MappingObjectStr>();
		
		for (MappingObjectStr map_mo1 : source2mo){
			//TODO why only ClassMappings?
			if (!map_mo1.isClassMapping())
				continue;
			for (MappingObjectStr map_mo2 : mo2target){
				
				if (!map_mo2.isClassMapping())
					continue;
				
				if (map_mo1.getIRIStrEnt2().equals(map_mo2.getIRIStrEnt1())){
		
					MappingObjectStr mapping = new MappingObjectStr(
							map_mo1.getIRIStrEnt1(), 
							map_mo2.getIRIStrEnt2(), 
							(map_mo1.getConfidence()+map_mo2.getConfidence())/2.0, MappingObjectStr.EQ, MappingObjectStr.CLASSES);
					
					composedMappings.add(mapping);
					//TODO addVote as part of statistics
				}
				}					
		} 
		
		return composedMappings;
	}
	
	/**
	 * Calls functionality from OutPutFilesManager to save a set of mappings to file. 
	 * The parameter 5 = all flat formats.
	 * @param composedMappings Set<MappingObjectStr> set of mappings
	 * @param outPath String 	Parent folder where the file will be  created
	 * @param ontoStr String	The short label representing the ontology in Bioportal
	 * @param onto1_iri String	file:<path to ontology> for source ontology
	 * @param onto2_iri String	file:<path to ontology> for target ontology
	 */
	public void saveComposedMappings(Set<MappingObjectStr>  composedMappings, String ontoName){
		// Save these mappings somewhere
		OutPutFilesManager mapSaver = new OutPutFilesManager();
		// 5 = AllFlatFormats
		try {
			mapSaver.createOutFiles(outPath + ontoName, 5, onto1_iri, onto2_iri);
			mapSaver.addMappings(composedMappings);
			mapSaver.closeAndSaveFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	public static void main(String[] args) throws IOException {
		RunMediatingOntologiesPipeline configReader = new RunMediatingOntologiesPipeline();
		configReader.getParentFolder(args);
		configReader.readConfigJSON();
		
		CreateComposedMappings compMapper = new CreateComposedMappings();
		compMapper.setAllPathsFromParent(configReader.parentPath);
		compMapper.setOnto1_IRI(configReader.sourceOntoPath);
		compMapper.setOnto2_IRI(configReader.targetOntoPath);
		String o1Name = compMapper.getOntologyNameFromFile(compMapper.onto1_iri);
		String o2Name = compMapper.getOntologyNameFromFile(compMapper.onto2_iri);

		/*
		 * Set up the ontology manager
		 */
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		// In case an import is broken
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		// Important to reassign value, see https://github.com/owlcs/owlapi/issues/503
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		onto_manager.setOntologyLoaderConfiguration(config);
		
		StoreMediatingOntologies moStorer = new StoreMediatingOntologies();
		CreateMappingsBetweenTwoOntologies simpleMapper = new CreateMappingsBetweenTwoOntologies();
		/*
		 * Find the list of mediating ontologies
		 */
		
		List<String> moList = moStorer.getOntologyListFromFile(compMapper.listFile);
		int countOnto = moList.size();
		System.out.println("There are " + countOnto + "mediating ontologies in the list");
		
		int counter = 0;
		for (String ontoStr: moList) {
			
			counter++;
			OWLOntology mo_i = null;
			System.out.println("Fetching ontology No.  " + counter + " label:  " +  ontoStr);
			// if the mediating ontology is missing or the mapping file exists, skip
			System.out.println(compMapper.basePath + ontoStr + ".owl");
			boolean isOntoThere = moStorer.checkOntoPath(ontoStr, compMapper.basePath, ".owl");
			boolean isMappingThere = moStorer.checkOntoPath(ontoStr, compMapper.outPath,".txt");
			if (isOntoThere == true && isMappingThere== false) {
				String mediOntoIRI = "file:" + compMapper.basePath + ontoStr + ".owl";
				String moName = compMapper.getOntologyNameFromFile(mediOntoIRI);
				System.out.println("Loading the mediating ontology " + mediOntoIRI);
				Set<MappingObjectStr> s2mMappings = simpleMapper.createMappings(compMapper.onto1_iri, mediOntoIRI);
				String mapFilePath1 = compMapper.midPath + o1Name + "_" + moName;
				simpleMapper.saveOntologyMappings(s2mMappings, mapFilePath1,compMapper.onto1_iri, mediOntoIRI);
				System.out.println("Saved mappings between source " + o1Name + " and medium " + moName);
				
				Set<MappingObjectStr> m2tMappings = simpleMapper.createMappings(mediOntoIRI,compMapper.onto2_iri);
				String mapFilePath2 = compMapper.midPath + moName + "_" + o2Name;
				simpleMapper.saveOntologyMappings(m2tMappings, mapFilePath2,mediOntoIRI, compMapper.onto2_iri);
				System.out.println("Saved mappings between medium " + moName + " and target " + o2Name);
				//TimeUnit.SECONDS.sleep(5);

				//CreateComposedMappings mapComposer = new CreateComposedMappings();
				Set<MappingObjectStr> composedMappings = compMapper.composeOntologyMappingsFromFile(mapFilePath1 + ".txt", mapFilePath2 + ".txt");
				System.out.println("There are " + composedMappings.size() + " composed mappings");
				compMapper.saveComposedMappings(composedMappings, moName);

				// flush memory?
				composedMappings = null;
				
			}else {
				if(isOntoThere == false) {
				System.out.println("Ontology " +  ontoStr + " not found, skipping");
				}
				if(isMappingThere == true) {
				System.out.println("Mapping file already existed for " +  ontoStr + " skipping");

				}
				
				continue;
			}

		}
		
	
		System.out.println("All storing of composed mappings is now complete");

		}
	}
		
		
	
