import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

public class DataParser {

	public final static String prefix = "pt";
	public final static String ns = "http://www.PTWproject.org/ontology#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "data.csv";

		OntModel model = ModelFactory.createOntologyModel();

		model.setNsPrefix(prefix, ns);

		addFile(filePath, model);

		model.write(System.out, "TURTLE");
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
				properties[i] = model.createProperty(model.getNsPrefixURI("pt") + headers[i].toLowerCase());
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
			String xsdDate = dmyToXSDate(values[0]);
			String xsdTime = values[1] + ":00";
			data.addProperty(properties[0], xsdDate);
			data.addProperty(properties[1], xsdTime);
			data.addProperty(model.getProperty(ns + "dateTime"), xsdDate + "T" + xsdTime);
			data.addProperty(model.getProperty(ns + "roadLaneMeasured"), model.getResource(sensor.getURI() + "_" + values[2]));
			for (int i = 3; i < values.length; i++) {
				Literal value = model.createTypedLiteral(values[i], "http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
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

}
