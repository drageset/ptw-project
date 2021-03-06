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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * The AirDataParser is used for parsing air pollution data from NILU,
 * it takes csv files and returns ttl files,
 * and can take file path as command line args.
 * An easy way to use it is to edit the filepath and output filepath in the beginning of main.
 */
public class AirDataParser {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {	
		String filePath = "AirCSV/adata.csv";
		String outputFilePath = "SemanticAirData/semanticAirData";
		if (args.length == 2){
			filePath = args[0];
			outputFilePath = args[1];
		} else if (args.length == 1) {
			filePath = "AirCSV/" + args[0];
			outputFilePath = "SemanticAirData/" + args[0];
		} else if (args.length > 0){
			System.out.println("WeatherDataParser takes one or two arguments: Either the filename of the file you want parsed from the AirCSV folder ( which will be used again for the output in the SemanticAirData folder), or the full filepath for input csv AND the full filepath for output ttl");
		}
		parseToTurtle(filePath, outputFilePath);
	}
	
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

	protected static void parseData(String filePath, OntModel model) throws FileNotFoundException, IOException {
		Vocab vocab = Vocab.getInstance();
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
			
			Resource airMeasurement = model.createResource(Vocab.ns + "AirMeasurement");
			Resource measurementType = model.createResource(Vocab.ns + pollutantMeasured + "Measurement");
			measurementType.addProperty(RDFS.subClassOf, airMeasurement);
			Resource sensorResource = model.createResource(Vocab.ns + sensorLocation +"_"+ pollutantMeasured +"_Sensor");
			
			Resource observation = model.createResource(Vocab.ssn + "Observation");
			measurementType.addProperty(OWL.equivalentClass, observation);			

			sensorResource.addProperty(RDFS.label, sensorLabel);
			sensorResource.addProperty(vocab.unitOfMeasurement, unit);
			
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				addData(line, model, measurementType, sensorResource, unit);
			}
			reader.close();
			//model.write(System.out, "TURTLE");

		} catch (Exception e) {
			e.printStackTrace();
		}

	} //end addFile

	private static void addData(String line, Model model, Resource measurementType, Resource sensor, String unit) {
		Vocab vocab = Vocab.getInstance();
		
		Resource data = model.createResource();
		data.addProperty(RDF.type, measurementType);
		data.addProperty(vocab.measuredBySensor, sensor);

		String[] values = line.split(";");
		if(values.length == 3){
			
			//Prepping strings for creating XSD date time/time literals
			String[] rawDateTimeStart = values[0].split(" ");
			String[] rawDateTimeEnd = values[1].split(" ");
			String xsdDateString = ParseUtils.airPolutionDate_to_XSDate( rawDateTimeStart[0] );
			
			String xsdDateTimeStartString = ParseUtils.airPolutionTime_to_XSDateTime( values[0] ) + ":00";
			String xsdTimeStartString = rawDateTimeStart[1] + ":00";
			
			String xsdDateTimeEndString = ParseUtils.airPolutionTime_to_XSDateTime( values[1] ) + ":00";
			String xsdTimeEndString = rawDateTimeEnd[1] + ":00";
			
			
			//Prepping XSD dateTime/time literals for creating OWL time instants and intervals
			Literal xsdDate = model.createTypedLiteral(xsdDateString, Vocab.xsd + "date");
			Literal xsdDateTimeStart = model.createTypedLiteral(xsdDateTimeStartString, Vocab.xsd + "dateTime");
			Literal xsdDateTimeEnd = model.createTypedLiteral(xsdDateTimeEndString, Vocab.xsd + "dateTime");
			Literal xsdTimeEnd = model.createTypedLiteral(xsdTimeEndString, Vocab.xsd + "time");
			Literal xsdTimeStart = model.createTypedLiteral(xsdTimeStartString, Vocab.xsd + "time");
			Literal measurementValue = model.createTypedLiteral(values[2].replace(",", "."), Vocab.xsd + "float");
			Literal unitLit = model.createLiteral(unit);
			
			//Prepping the instants for the interval creation
			Resource instantStart = model.createResource();
			instantStart.addProperty(vocab.inXSDDateTime, xsdDateTimeStart);
			Resource instantEnd = model.createResource();
			instantEnd.addProperty(vocab.inXSDDateTime, xsdDateTimeEnd);
			
			//creating the interval
			Resource interval = model.createResource(); // the owl time interval in which the measurement is made
			interval.addProperty(vocab.owlBeginning, instantStart);
			interval.addProperty(vocab.owlEnd, instantEnd);
			
			//adding final properties to the datapoint
			data.addProperty(vocab.date, xsdDate);
			data.addProperty(vocab.measuredTimeInterval, interval); //the data has a measuredTimeInterval this interval
			data.addProperty(vocab.endTime, xsdTimeEnd);
			data.addProperty(vocab.startTime, xsdTimeStart);
			data.addProperty(vocab.valueMeasured, measurementValue);
			data.addProperty(vocab.unitOfMeasurement, unitLit);

		}
	} //end addData

}
