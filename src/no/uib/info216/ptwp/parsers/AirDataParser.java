package no.uib.info216.ptwp.parsers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class AirDataParser {
	
	public final static String prefix = "ptw";
	public final static String ns = "http://www.ptwproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "airdata.csv";
		String outputFilePath = "semanticAirData";
		parseToTurtle(filePath, outputFilePath);
	}
	
	public static void parseToTurtle(String filePath, String outputFilePath){
		String notation = "TURTLE";
		outputFilePath += ".ttl";
		OntModel model = ModelFactory.createOntologyModel(); 

		model.setNsPrefix(prefix, ns);

		try {
			parseData(filePath, model);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ParseUtils.writeModelToFile(model, outputFilePath, notation);
	}

	protected static void parseData(String filePath, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePath);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(csvFile));

			String[] header = reader.readLine().split(";");
			String sensorLabel = header[2];
			String[] sensorDetails = header[2].replaceAll(", ","_").split(" \\| ");
			String sensorLocation = sensorDetails[0];
			String pollutantMeasured = sensorDetails[1];
			String unit = sensorDetails[2];
			
			Property unitOfMeasurement = model.createProperty(ns + "unitOfMeasurement");
			@SuppressWarnings("unused")
			Property measuredBySensor = model.createProperty(ns + "measuredBySensor");
			Property startDateTime = model.createProperty(ns + "startDateTime");
			Property endDateTime = model.createProperty(ns + "endDateTime");
			Property endTime = model.createProperty(ns + "endTime");
			Property startTime = model.createProperty(ns + "startTime");
			Property mgpsm = model.createProperty(ns + "microGramsPerMeterCubed");
			Property[] properties = {startDateTime, endDateTime, mgpsm};
			
			Resource measurementType = model.createResource(ns + pollutantMeasured + "Measurement");
			Resource sensorResource = model.createResource(ns + sensorLocation +"_"+ pollutantMeasured +"_Sensor");
			
			sensorResource.addProperty(RDFS.label, sensorLabel);
			sensorResource.addProperty(unitOfMeasurement, unit);
			
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, measurementType, sensorResource);
			}
			reader.close();
			model.write(System.out, "TURTLE");

		} catch (Exception e) {
			e.printStackTrace();
		}

	} //end addFile

	private static void addData(String line, Model model, Property[] properties, Resource measurementType, Resource sensor) {
		Resource data = model.createResource();
		data.addProperty(RDF.type, measurementType);
		data.addProperty(model.getProperty(ns + "measuredBySensor"), sensor);

		String[] values = line.split(";");
		if(values.length == 3){
			String xsdDateTimeStartString = ParseUtils.airPolutionTime_to_XSDateTime( values[0] );
			String xsdDateTimeEndString = ParseUtils.airPolutionTime_to_XSDateTime( values[1] );
			String xsdTimeEndString = ParseUtils.airPolutionTime_to_XSDTime(values[1]);
			String xsdTimeStartString = ParseUtils.airPolutionTime_to_XSDTime(values[1]);
			Literal xsdDateTimeStart = model.createTypedLiteral(xsdDateTimeStartString, xsd + "dateTime");
			Literal xsdDateTimeEnd = model.createTypedLiteral(xsdDateTimeEndString, xsd + "dateTime");
			Literal xsdTimeEnd = model.createTypedLiteral(xsdTimeEndString, xsd + "time");
			Literal xsdTimeStart = model.createTypedLiteral(xsdTimeStartString, xsd + "time");
			Literal measurementValue = model.createTypedLiteral(values[2].replace(",", "."), xsd + "float");
			data.addProperty(properties[0], xsdDateTimeStart);
			data.addProperty(properties[1], xsdDateTimeEnd);
			data.addProperty(model.getProperty(ns + "endTime"), xsdTimeEnd);
			data.addProperty(model.getProperty(ns + "startTime"), xsdTimeStart);
			data.addProperty(properties[2], measurementValue);

		}
	} //end addData

}
