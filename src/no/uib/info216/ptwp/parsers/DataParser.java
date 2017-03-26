package no.uib.info216.ptwp.parsers;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;

interface DataParser {

	/**
	 * This method takes as input the filepath of a csv file that is compatible with the parser, 
	 * and an Ontology Model to which the file is to be parsed in to. 
	 * The method then parses the data from the CSV file into the ontology model.
	 * @param filePath is the filepath of the csv file
	 * @param model is the ontology model to which the data should be written
	 */
	public void parseData(String filePath, OntModel model) throws FileNotFoundException, IOException;
	
}
