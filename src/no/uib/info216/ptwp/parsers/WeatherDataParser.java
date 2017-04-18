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
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class WeatherDataParser {
	public final static String prefix = "ptw";
	public final static String ns = "http://www.ptwproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";
	public final static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	public final static String qb = "http://purl.org/linked-data/cube#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "wdata.csv";
		String outputFilePath = "semanticWData";
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

		model.setNsPrefix(prefix, ns);
		model.setNsPrefix("ssn", ssn);
		model.setNsPrefix("qb", qb);
		

		try {
			parseData(filePath, model);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ParseUtils.writeModelToFile(model, outputFilePath, notation);
	}

	private static void parseData (String filePath, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePath);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(csvFile));
			
			//Tre linjer med drit i toppen
			reader.readLine();
			reader.readLine();
			reader.readLine();
			
			String[] stHeaders = reader.readLine().split(";");
			
			Resource dataSet = model.createResource(ns + "DataSet");
			Resource sensorClass = model.createResource( ns + "Sensor" );
			Resource weatherSensorClass = model.createResource( ns + "WeatherSensor" );
			weatherSensorClass.addProperty(RDFS.subClassOf, sensorClass);
			sensorClass.addProperty(OWL.sameAs, ssn + "Sensor");

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
			weatherMeasurementClass.addProperty(OWL.sameAs, ssn + "Observation");	

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
//				String stIndividual = (stNum + "_" + stName).replaceAll(" ", "").replaceAll("-", "_");
				Individual wSensor = model.createIndividual(ns + stNum, weatherSensorClass);
				Literal nameLabel = model.createLiteral(stName);
				wSensor.addLabel(nameLabel);
				
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
			for (int j = 0; j < 38; j++) {
				reader.readLine();
			}

			String[] headers = reader.readLine().split(";");
			Property[] properties = new Property[headers.length];
			for (int i=0; i<headers.length; i++){
				properties[i] = model.createProperty(model.getNsPrefixURI(prefix) + headers[i].toLowerCase());
				if(i > 1){
					properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
				}
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, dataSet, weatherMeasurementClass);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} //end parseData

	
	private static void addData(String line, Model model, Property[] properties, Resource Dataset, Resource measurementType) {
		Resource data = model.createResource();
		data.addProperty(RDF.type, measurementType);
		String[] values = line.split(";");
		//ingen getLiteral()-metode, funker getResource()?
		data.addProperty(model.getProperty(ns + "measuredByStation"), model.getResource(ns) + values[0]);

		String[] rawDate = values[1].split("-");
//		System.out.println(rawDate[0]);
//		System.out.println(rawDate[1]);
		String xsdDateString = ParseUtils.dmyToXSDate(rawDate[0]);
		String xsdTimeString = rawDate[1];
		String xsdDateTimeString = xsdDateString + "T" + xsdTimeString;
		Literal xsdDateTime = model.createTypedLiteral(xsdDateTimeString, xsd + "dateTime");
		data.addProperty(properties[1], xsdDateTime);
		
		for (int i = 2; i < values.length; i++) {
			Literal value = model.createTypedLiteral(values[i], xsd + "nonNegativeInteger");
			data.addLiteral(properties[i], value);
		}
			
		
	} //end addData
}// end class
