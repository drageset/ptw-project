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

/**
 * The TrafficDataParser is used for parsing traffic data from Vegvesenet,
 * it takes csv files and returns ttl files,
 * and can take file path as command line args.
 * An easy way to use it is to edit the filepath and output filepath in the beginning of main.
 */
public class TrafficDataParser {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "TrafficCSV/tdata.csv"; //location for traffic data csv file you wish to parse if filepath is not specified in args[0]
		String outputFilePath = "SemanticTrafficData/semanticTrafficData"; //default location for ttl output if output filepath is not specified in args[1]
		if (args.length == 2){
			filePath = args[0];
			outputFilePath = args[1];
		} else if (args.length == 1) {
			filePath = "TrafficCSV/" + args[0];
			outputFilePath = "SemanticTrafficData/" + args[0];
		} else if (args.length > 0){
			System.out.println("TrafficDataParser takes one or two arguments: Either the filename of the file you want parsed from the TrafficCSV folder( which will be used again for the output in the SemanticTrafficData folder), or the full filepath for input csv AND the full filepath for output ttl");
		}
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
		model.setNsPrefix("time", Vocab.time);
		

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
			line1[5] = line1[5].replaceAll(" og ", ", ");
			String[] felt1 = line1[5].substring(5).split(", ");
			String felt1retning = line1[6];

			String[] line2 = reader.readLine().split(";");
			line2[5] = line2[5].replaceAll(" og ", ", ");
			String[] felt2 = line2[5].substring(5).split(", ");
			String felt2retning = line2[6];

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
				properties[i] = model.createProperty(model.getNsPrefixURI(Vocab.prefix) + headers[i]);
				if (i > 2) {
					properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
				}
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, sensorResource);
			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	} //end parseData


	private static void addData(String line, Model model, Property[] properties,
			Resource sensor) {
		Vocab vocab = Vocab.getInstance();
		Resource data = model.createResource();
		data.addProperty(RDF.type, vocab.trafficMeasurementClass);
		data.addProperty(model.getProperty(Vocab.ns + "measuredBySensor"), sensor);

		String[] values = line.split(";");
		if (values.length == 9) {
			
			String xsdDateString = ParseUtils.dmyToXSDate(values[0]);
			String xsdTimeEndString = values[1] + ":00";
			String xsdDateTimeEndString = xsdDateString + "T" + xsdTimeEndString;
			String xsdDateTimeStartString = ParseUtils.calculateStartDateTime(xsdDateTimeEndString); 
			String xsdTimeStartString = ParseUtils.calculateStartTime(values[1] + ":00");

			Literal xsdDate = model.createTypedLiteral(xsdDateString, Vocab.xsd + "date");
			Literal xsdTimeEnd = model.createTypedLiteral(xsdTimeEndString, Vocab.xsd + "time");
			Literal xsdTimeStart = model.createTypedLiteral(xsdTimeStartString + ":00", Vocab.xsd + "time");
			Literal xsdDateTimeEnd = model.createTypedLiteral(xsdDateTimeEndString, Vocab.xsd + "dateTime");
			Literal xsdDateTimeStart = model.createTypedLiteral(xsdDateTimeStartString + ":00", Vocab.xsd + "dateTime");
			
			Resource instantStart = model.createResource();
			instantStart.addProperty(vocab.inXSDDateTime, xsdDateTimeStart);
			Resource instantEnd = model.createResource();
			instantEnd.addProperty(vocab.inXSDDateTime, xsdDateTimeEnd);
			
			Resource interval = model.createResource();
			interval.addProperty(vocab.owlBeginning, instantStart);
			interval.addProperty(vocab.owlEnd, instantEnd);
			data.addProperty(vocab.startTime, xsdTimeStart);
			data.addProperty(vocab.measuredTimeInterval, interval);
			data.addProperty(vocab.endTime, xsdTimeEnd);
			data.addProperty(properties[0], xsdDate);
			data.addProperty(model.getProperty(Vocab.ns + "dateTime"), xsdDateTimeEnd);
			data.addProperty(model.getProperty(Vocab.ns + "roadLaneMeasured"),
					model.getResource(sensor.getURI() + "_" + values[2]));
			for (int i = 3; i < values.length; i++) {
				Literal value = model.createTypedLiteral(values[i], Vocab.xsd + "nonNegativeInteger");
				data.addLiteral(properties[i], value);
			}
		}
	} // end addData
}