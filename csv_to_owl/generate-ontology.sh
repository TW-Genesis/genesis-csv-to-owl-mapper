#!/bin/bash

# Export all environment variables
. app.config.sh

# A temporary triple file for terms is generated using 
# the [rmlmapper](https://github.com/RMLio/rmlmapper-java) tool 
# with input as the csv files(entities.csv, relations.csv, instances.csv).

terms_file=$(mktemp)
java -jar rmlmapper.jar --mapping mapping.ttl --output "$terms_file" --serialization turtle

# The annotations of all the terms are fetched. 
# The fetch-annotations.sh script can be used to fetch annoations:
# . fetch-annotations.sh <terms-file> <folder-where-the-output-annotations-will-be-stored> 
# The above script internally uses the [Ontofox](https://ontofox.hegroup.org/) tool for the same.
# The csv files should have a column containing the IRI of the ontology graph which contains annotations for the term.
# The output of this step contains separate files for all the terms containing respective annotations.

annotation_dir=$(mktemp -d)
. fetch-annotations.sh csv/entities.csv $annotation_dir
. fetch-annotations.sh csv/relations.csv $annotation_dir
. fetch-annotations.sh csv/instances.csv $annotation_dir


# Finally the ontology is generated by combining all the files generated in previous steps.
# The custom tool `merge-and-generate-ontology` built using apache jena api can be used to merge the ontologies.

annotation_file_args=""

for ann_file in $annotation_dir/*.annotations.xml;
do 
    annotation_file_args="$annotation_file_args --ann $ann_file"; 
done

java -jar merge-and-generate-ontology-1.0-SNAPSHOT-all.jar --iri=$generated_ontology_IRI --rdfs="$terms_file" -fn genesis.owl $annotation_file_args
