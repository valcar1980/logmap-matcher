package uk.ac.ox.krr.logmap2.mediating_ontologies;

interface ExperimentInterface {
	
	public void readExperimentConfigs(String experimentPath);
	public void loadExperimentInput();
	public void runExperimentPipeline();
	public void storeExperimentOutput();
	public void saveExperimentDataLineageToFile();
	public void printExperimentStatsToScreen();
	public void saveExperimentLogsToFile();
}