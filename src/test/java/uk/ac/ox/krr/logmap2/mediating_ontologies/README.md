## How to use the mediating ontologies package

### Get your input data
Our default example starts with the human and mouse ontology
You can download them manually at OAEI
human.owl
mouse.owl

### Steps

1. RunMediatingOntologies

This gets the top 10 mediating ontologies for the source and target ontology
Change the hard-coded paths to match what you have downloaded
Add arguments to the Run Configuration for the file -Xms500M -Xmx15G -DentityExpansionLimit=10000000 --add-opens=java.base/java.lang=ALL-UNNAMED

2. CreateComposedMappings
(Same setup as before)