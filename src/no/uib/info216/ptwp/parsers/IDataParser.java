package no.uib.info216.ptwp.parsers;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;

interface IDataParser {

	/**
	 * Reads a file and puts the content into a model.
	 * This method takes as input the filepath of a csv file that is compatible with the parser, 
	 * and an Ontology Model to which the file is to be parsed in to. 
	 * The method then parses the data from the CSV file into the ontology model.
	 * @param filePath is the filepath of the csv file
	 * @param model is the ontology model to which the data should be written
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void parseData(String filePath, OntModel model) throws FileNotFoundException, IOException;
	
	/**
	 * Takes a CSV file, parses it, and writes it back to a turtle file
	 * @param filePath is the name of the csv filepath
	 * @param outputFilePath is the name of the ttl filepath
	 */
	public void parseToTurtle(String filePath, String outputFilePath);
	
}
