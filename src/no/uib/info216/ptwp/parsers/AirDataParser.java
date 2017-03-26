package no.uib.info216.ptwp.parsers;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

public class AirDataParser {
	
	public final static String prefix = "ptw";
	public final static String ns = "http://www.PTWproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";

	public static void main(String[] args) {
		String filePath = "airdata.csv";
		String outputFilePath = "semanticAirData.ttl";
		String notation = "TURTLE";

		OntModel model = ModelFactory.createOntologyModel();
		model.setNsPrefix(prefix, ns);
		
		parseData(filePath, model);
		ParseUtils.writeModelToFile(model, outputFilePath, notation);
	}

	private static void parseData(String filePath, OntModel model) {
		// TODO Auto-generated method stub
		
	}

}
