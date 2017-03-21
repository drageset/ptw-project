import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.jena.datatypes.xsd.XSDDateTime;
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
		String outputFilePath = "semanticData.ttl";
		String notation = "TURTLE";
		
		OntModel model = ModelFactory.createOntologyModel(); 

		model.setNsPrefix(prefix, ns);

		addFile(filePath, model);

		writeModelToFile(model, outputFilePath, notation);
		
		model.write(System.out, "TURTLE");
	}
	
	public static void parse(String filePath, String outputFilePath){
		String notation = "TURTLE";

		OntModel model = ModelFactory.createOntologyModel(); 

		model.setNsPrefix(prefix, ns);

		try {
			addFile(filePath, model);
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeModelToFile(model, outputFilePath, notation);
	}

	private static void addFile (String filePath, OntModel model) throws FileNotFoundException, IOException {
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
			String xsdDateString = dmyToXSDate(values[0]);
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
				//data.addLiteral(properties[i], Integer.parseInt(values[i]));
			}	
		}
	} //end addData

	/**
	 * Gjør dato på formatet dd.mm.yyyy (dmy) om til xsd:date format, som er yyyy-mm-dd
	 * @param dmy string
	 * @return xsd:date format string
	 */
	private static String dmyToXSDate(String dmy){
		try {
			return calToXSDate(dmyToCal(dmy));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "ErrorFailedToParse";
	}
	
	/**
	 * Gjør dato på formatet xsd:datetime om til et objekt av typen XSDDateTime
	 * @param xsd:dateTime string
	 * @return XSDDateTime objekt
	 */
	@SuppressWarnings("unused")
	private static XSDDateTime toXSDateTime(String xsdString){
		Calendar cal;
		try {
			cal = Calendar.getInstance();
			SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
			cal.setTime(xsdFormat.parse(xsdString));
			return new XSDDateTime(cal);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gjør dato på formatet dd.mm.yyyy (dmy) om til Calendar
	 * @param dmy string
	 * @return Date
	 * @throws ParseException 
	 */
	private static Calendar dmyToCal(String dmy) throws ParseException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		cal.setTime(sdf.parse(dmy));
		return cal;  
	}

	/**
	 * Gjør en calendar om til en streng med formatet til xsd:date
	 * @param cal
	 * @return xsd:date format string
	 */
	private static String calToXSDate(Calendar cal) {
		Date time = cal.getTime();
		SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		String xsdDate = xsdFormat.format(time);
		return xsdDate;
	}
	
	/**
	 * Writes a model to a file in a chosen notation
	 * @param model The RDF model that you wish to write to a file
	 * @param filename The filename of the file that you wish to write the RDF model to
	 * @param notation The notation (i.e. TURTLE, JSON-LD) that you wish the file to be written in
	 */
	private static void writeModelToFile(Model model, String filename, String notation){
		try{
		    PrintWriter writer = new PrintWriter(filename, "UTF-8");
		    model.write(writer, notation);
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
