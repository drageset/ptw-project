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

public class WeatherDataParser {
	public final static String prefix = "ptw";
	public final static String ns = "http://www.PTWproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "wdata.csv";
		String outputFilePath = "semanticWData.ttl";
		String notation = "TURTLE";

		OntModel model = ModelFactory.createOntologyModel(); 

		model.setNsPrefix(prefix, ns);

		addFile(filePath, model);

		ParseUtils.writeModelToFile(model, outputFilePath, notation);

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

		ParseUtils.writeModelToFile(model, outputFilePath, notation);
	}

	private static void addFile (String filePath, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePath);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(csvFile));
			
			//Tre linjer med drit i toppen
			reader.readLine();
			reader.readLine();
			reader.readLine();
			
			String[] stHeaders = reader.readLine().split(";");
			
			Resource sensorClass = model.createResource( ns + "Sensor" );
			Resource weatherSensorClass = model.createResource( ns + "WeatherSensor" );
			weatherSensorClass.addProperty(RDFS.subClassOf, sensorClass);

			Property inServiceSince = model.createProperty(ns + stHeaders[2]);
			Property inServiceUntil = model.createProperty(ns + stHeaders[3]);
			inServiceSince.addProperty(RDFS.domain, weatherSensorClass);
			inServiceUntil.addProperty(RDFS.domain, weatherSensorClass);
			inServiceSince.addProperty(RDFS.range, XSD.gYearMonth);
			// Alle feltene for denne verdien er blanke, så lar range være uspesifisert enn så lenge
//			inServiceUntil.addProperty(RDFS.range, XSD.gYearMonth);

			Resource measurementClass = model.createResource(ns + "Measurement");
			Resource weatherMeasurementClass = model.createResource(ns + "WeatherMeasurement");
			weatherMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);

			Property dateTime = model.createProperty(ns + "dateTime");
			dateTime.addProperty(RDFS.range, XSD.dateTime);

			Property masl = model.createProperty(ns + stHeaders[4]);
			masl.addProperty(RDFS.domain, weatherSensorClass);
			masl.addProperty(RDFS.range, XSD.nonNegativeInteger);

			Property inPrincipality = model.createProperty(ns + stHeaders[5]);
			Property inCounty = model.createProperty(ns + stHeaders[6]);
			Property inRegion = model.createProperty(ns + stHeaders[7]);

			// Blokk med data om værstasjonene
			for (int i = 0; i < 6; i++) {
				String[] stvalues = reader.readLine().split(";");
				
				//Slår sammen stasjonnens navn og nummmer for å lage URI
				//Sikkert lurt å også legge til nummer og eller navn hver for seg som properties
				String stNum = stvalues[0];
				String stName = stvalues[1];
				String stIndividual = (stNum + "_" + stName).replaceAll(" ", "").replaceAll("-", "_");
				Individual wSensor = model.createIndividual(ns + stIndividual, weatherSensorClass);
				
				//I drift fra
				String stMonth = stvalues[2].substring(0, 3);
				String stYear = stvalues[2].substring(4);
				String stYearMonth = ParseUtils.fixDateFormat(stMonth, stYear);
				Literal yearMonth = model.createTypedLiteral(stYearMonth, xsd + "gYearMonth");
				wSensor.addProperty(inServiceSince, yearMonth);
				
				//I drift til (evt. skippe dette feltet, da ingen av stasjonene har noe data der)
				wSensor.addProperty(inServiceUntil, stvalues[3]);
				
				//Høyde over havet
				wSensor.addLiteral(masl, Integer.parseInt(stvalues[4]));
				
				//Lokasjon
				wSensor.addProperty(inPrincipality, stvalues[5]);
				wSensor.addProperty(inCounty, stvalues[6]);
				wSensor.addProperty(inRegion, stvalues[7]);	
			}

			//38 linjer med drit
			for (int j = 1; j < 38; j++) {
				reader.readLine();
			}


//			String[] headers = reader.readLine().split(";");
//			Property[] properties = new Property[headers.length];
//			for (int i=0; i<headers.length; i++){
//				properties[i] = model.createProperty(model.getNsPrefixURI(prefix) + headers[i].toLowerCase());
//				if(i > 2){
//					properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
//				}
//			}

//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				addData(line, model, properties, weatherMeasurementClass, sensor);
//			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	} //end addFile

	
	//Kopi fra trafficParser, ikke endret noe på enda
	@SuppressWarnings("unused")
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
				//data.addLiteral(properties[i], Integer.parseInt(values[i]));
			}	
		}
	} //end addData
}// end class
