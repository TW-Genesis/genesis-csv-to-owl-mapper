package org.genesis.mergeandgenerateontology;

import org.apache.commons.cli.*;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        Options options = new Options();
        Option IRIOpt = Option.builder("iri").longOpt("iri")
                .argName("iri")
                .hasArg()
                .required(true)
                .desc("IRI of the generated ontology").build();
        options.addOption(IRIOpt);

        Option rdfsTermsFileOpt = Option.builder("rdfs").longOpt("rdfs")
                .argName("rdfs")
                .hasArg()
                .required(true)
                .desc("a turtle file containing rdfs terms").build();
        options.addOption(rdfsTermsFileOpt);

        Option annotationsFilesOpt = Option.builder("ann").longOpt("annotations")
                .argName("annotations")
                .hasArg()
                .required(true)
                .desc("comma separated list of owl files containing annotations for the rdfs terms").build();
        options.addOption(annotationsFilesOpt);

        Option fileNameOpt = Option.builder("fn").longOpt("filename")
                .argName("filename")
                .hasArg()
                .required(false)
                .desc("output file name").build();
        options.addOption(fileNameOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            String rdfsTermsFileName = cmd.getOptionValue("rdfs");
            System.out.println("rdfs file:" + rdfsTermsFileName);
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            ontModel.read(new File(rdfsTermsFileName).toURI().toString(), "TURTLE");

            for (String annotationFileName : cmd.getOptionValues("ann")) {
                System.out.println("annotations file:" + annotationFileName);
                ontModel.read(new File(annotationFileName).toURI().toString());
            }

            String IRIStr = cmd.getOptionValue("iri");
            System.out.println("ontology IRI set to:" + IRIStr);
            modifyOntologyName(ontModel, IRIStr);

            String fileName = cmd.getOptionValue("fn", "output.owl");
            FileWriter fileWriter = new FileWriter(fileName);
            ontModel.write(fileWriter, "TTL");
            fileWriter.close();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(0);
        }
    }

    private static void modifyOntologyName(OntModel ontModel, String IRIStr) {
        String modifyOntologyNameQueryTemplate = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                DELETE { ?ont ?anyPredicate  ?anyObject }
                INSERT { <%s> rdf:type owl:Ontology }
                WHERE
                {
                    ?ont ?anyPredicate  ?anyObject ; rdf:type owl:Ontology
                }""";
        UpdateAction.parseExecute(String.format(modifyOntologyNameQueryTemplate, IRIStr), ontModel);
    }

}
