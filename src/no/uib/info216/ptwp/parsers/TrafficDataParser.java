package no.uib.info216.ptwp.parsers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class TrafficDataParser {

	public final static String prefix = "ptw";
	public final static String ns = "http://www.PTWproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "data.csv";
		String outputFilePath = "semanticData";
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

			String[] line1 = reader.readLine().split(";");
			line1[4] = line1[4].replaceAll(" og ", ", ");
			String[] felt1 = line1[4].substring(5).split(", ");
			String felt1retning = line1[5];

			String[] line2 = reader.readLine().split(";");
			line2[4] = line2[4].replaceAll(" og ", ", ");
			String[] felt2 = line2[4].substring(5).split(", ");
			String felt2retning = line2[5];

			String[] line3 = reader.readLine().split(";");
			String tellepunktNavn = line3[0].substring(12);
			String tellepunktPropName = tellepunktNavn.replaceAll(" ", "").replaceAll("-", "_");

			String[] line4 = reader.readLine().split(";");
			String label = line4[0];


			Resource sensorClass = model.createResource( ns + "Sensor" );
			Resource trafficSensorClass = model.createResource( ns + "TrafficSensor" );
			trafficSensorClass.addProperty(RDFS.subClassOf, sensorClass);
			Individual sensor = model.createIndividual( ns + tellepunktPropName, trafficSensorClass);
			Literal labelLit = model.createLiteral(label);
			sensor.addLabel(labelLit);

			Resource feltClass = model.createResource(ns + "RoadLane");
			Property roadLaneDirection = model.createProperty(ns + "roadLaneDirection");
			roadLaneDirection.addProperty(RDFS.domain, feltClass);

			Property measuresRoadLane = model.createProperty(ns + "measuresRoadLane");
			measuresRoadLane.addProperty(RDFS.domain, trafficSensorClass);

			Resource measurementClass = model.createResource(ns + "Measurement");
			Resource trafficMeasurementClass = model.createResource(ns + "TrafficMeasurement");
			trafficMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);
			
			Property dateTime = model.createProperty(ns + "dateTime");
			dateTime.addProperty(RDFS.range, XSD.dateTime);

			for(String felt : felt1){
				Individual laneres = model.createIndividual(ns + tellepunktPropName + "_" + felt, feltClass);
				laneres.addProperty(roadLaneDirection, felt1retning);
				sensor.addProperty(measuresRoadLane, laneres);
			}
			for (String felt : felt2) {
				Individual laneres = model.createIndividual(ns + tellepunktPropName + "_" + felt, feltClass); 
				laneres.addProperty(roadLaneDirection, felt2retning);
				sensor.addProperty(measuresRoadLane, laneres);
			}

			String[] headers = reader.readLine().split(";");
			Property[] properties = new Property[headers.length];
			for (int i=0; i<headers.length; i++){
				properties[i] = model.createProperty(model.getNsPrefixURI(prefix) + headers[i].toLowerCase());
				if(i > 2){
					properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
				}
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, trafficMeasurementClass, sensor);
			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	} //end addFile

	private static void addData(String line, Model model, Property[] properties, Resource measurementType, Resource sensor) {
		Resource data = model.createResource();
		data.addProperty(RDF.type, measurementType);
		data.addProperty(model.getProperty(ns + "measuredBySensor"), sensor);

		String[] values = line.split(";");
		if(values.length == 9){
			String xsdDateString = ParseUtils.dmyToXSDate(values[0]);
			String xsdTimeString = values[1] + ":00";
			String xsdDateTimeString = xsdDateString + "T" + xsdTimeString;
			Literal xsdDate = model.createTypedLiteral(xsdDateString, xsd + "date");
			Literal xsdTime = model.createTypedLiteral(xsdTimeString, xsd + "time");
			Literal xsdDateTime = model.createTypedLiteral(xsdDateTimeString, xsd + "dateTime");
			data.addProperty(properties[0], xsdDate);
			data.addProperty(properties[1], xsdTime);
			data.addProperty(model.getProperty(ns + "dateTime"), xsdDateTime);
			data.addProperty(model.getProperty(ns + "roadLaneMeasured"), model.getResource(sensor.getURI() + "_" + values[2]));
			for (int i = 3; i < values.length; i++) {
				Literal value = model.createTypedLiteral(values[i], xsd + "nonNegativeInteger");
				data.addLiteral(properties[i], value);
			}	
		}
	} //end addData
}
