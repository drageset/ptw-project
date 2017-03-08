import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;

public class dataParser {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "data.csv";
		
		String curDir = System.getProperty("user.dir");
		System.out.println("Current sys dir: " + curDir);
		
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", "http://www.PTWproject.org/ontology#");
		
		addFile("data.csv", model);
		
	 	model.write(System.out, "TURTLE");
	}
	
	private static void addFile (String fileName, Model model) throws FileNotFoundException, IOException {
		File csvFile = new File(fileName);
		System.out.println("data filepath: " + csvFile.getAbsolutePath());
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(csvFile));
			String[] headers = reader.readLine().split(";");
			Property[] properties = new Property[headers.length];
			for (int i=0; i<headers.length; i++)
				properties[i] = model.createProperty(model.getNsPrefixURI("") + headers[i].toLowerCase());
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties);
			}
		 	reader.close();
		 	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void addData(String line, Model model, Property[] properties) {
		Resource person = model.createResource();
		person.addProperty(RDF.type, FOAF.Person);
		
		String[] values = line.split(";");
		for (int i=0; i<values.length; i++) {
			person.addProperty(properties[i], values[i]);
		}	
	}
}
