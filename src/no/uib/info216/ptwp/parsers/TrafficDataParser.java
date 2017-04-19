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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "tdata.csv";
		String outputFilePath = "semanticTrafficData";
		parseToTurtle(filePath, outputFilePath);
	}
	
	/**
	 * This method parses information from a traffic data CSV file and writes it out in a turtle file
	 * @param filePath of the original CSV file
	 * @param outputFilePath including name of the new TURTLE file, not included ".ttl" file ending.
	 */
	public static void parseToTurtle(String filePath, String outputFilePath){

		String notation = "TURTLE";
		outputFilePath += ".ttl";
		OntModel model = ModelFactory.createOntologyModel();

		model.setNsPrefix(Vocab.prefix, Vocab.ns);
		model.setNsPrefix("ssn", Vocab.ssn);
		model.setNsPrefix("qb", Vocab.qb);
		

		try {
			parseData(filePath, model);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ParseUtils.writeModelToFile(model, outputFilePath, notation);
	}

	/**
	 * This method reads the CSV files containing data about traffic from Vegvesenet, 
	 * and turns the information into sensible triples according to our ontology.
	 * @param filePath is the input CSV file that is read from.
	 * @param model is the model in which we add the triples about our data.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static void parseData(String filePath, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePath);
		BufferedReader reader;
		Vocab vocab = Vocab.getInstance();
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

			Individual sensorResource = model.createIndividual(Vocab.ns + tellepunktPropName, vocab.trafficSensorClass);
			Literal labelLit = model.createLiteral(label);
			sensorResource.addLabel(labelLit);

			for (String felt : felt1) {
				Individual laneres = model.createIndividual(Vocab.ns + tellepunktPropName + "_" + felt, vocab.feltClass);
				laneres.addProperty(vocab.roadLaneDirection, felt1retning);
				sensorResource.addProperty(vocab.measuresRoadLane, laneres);
			}
			for (String felt : felt2) {
				Individual laneres = model.createIndividual(Vocab.ns + tellepunktPropName + "_" + felt, vocab.feltClass);
				laneres.addProperty(vocab.roadLaneDirection, felt2retning);
				sensorResource.addProperty(vocab.measuresRoadLane, laneres);
			}

			String[] headers = reader.readLine().split(";");
			Property[] properties = new Property[headers.length];
			for (int i = 0; i < headers.length; i++) {
				properties[i] = model.createProperty(model.getNsPrefixURI(Vocab.prefix) + headers[i].toLowerCase());
				if (i > 2) {
					properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
				}
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, vocab.dataSet, sensorResource);
			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	} //end parseData


	private static void addData(String line, Model model, Property[] properties, Resource dataSet,
			Resource sensor) {
		Vocab vocab = Vocab.getInstance();
		Resource data = model.createResource();
		data.addProperty(RDF.type, vocab.trafficMeasurementClass);
		data.addProperty(model.getProperty(Vocab.ns + "measuredBySensor"), sensor);
		data.addProperty(model.getProperty(Vocab.ns + "belongsToDataSet"), dataSet);

		String[] values = line.split(";");
		if (values.length == 9) {
			String xsdDateString = ParseUtils.dmyToXSDate(values[0]);
			String xsdTimeString = values[1] + ":00";
			String xsdDateTimeString = xsdDateString + "T" + xsdTimeString;
			Literal xsdDate = model.createTypedLiteral(xsdDateString, Vocab.xsd + "date");
			Literal xsdTime = model.createTypedLiteral(xsdTimeString, Vocab.xsd + "time");
			Literal xsdDateTime = model.createTypedLiteral(xsdDateTimeString, Vocab.xsd + "dateTime");
			data.addProperty(properties[0], xsdDate);
			data.addProperty(properties[1], xsdTime);
			data.addProperty(model.getProperty(Vocab.ns + "dateTime"), xsdDateTime);
			data.addProperty(model.getProperty(Vocab.ns + "roadLaneMeasured"),
					model.getResource(sensor.getURI() + "_" + values[2]));
			for (int i = 3; i < values.length; i++) {
				Literal value = model.createTypedLiteral(values[i], Vocab.xsd + "nonNegativeInteger");
				data.addLiteral(properties[i], value);
			}
		}
	} // end addData
}
