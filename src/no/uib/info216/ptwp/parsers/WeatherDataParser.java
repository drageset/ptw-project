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

	private static void parseData (String filePath, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePath);
		BufferedReader reader;
		Vocab vocab = Vocab.getInstance();
		try {
			reader = new BufferedReader(new FileReader(csvFile));

			//Tre linjer med drit i toppen
			reader.readLine();
			reader.readLine();
			reader.readLine();

			String[] stHeaders = reader.readLine().split(";");


			//			Property inServiceUntil = model.createProperty(ns + stHeaders[3]);
			//			inServiceUntil.addProperty(RDFS.domain, weatherSensorClass);
			// Alle feltene for denne verdien er blanke, så lar range være uspesifisert enn så lenge
			//			inServiceUntil.addProperty(RDFS.range, XSD.gYearMonth);


			// Blokk med data om værstasjonene
			for (int i = 0; i < 6; i++) {
				String[] stvalues = reader.readLine().split(";");

				//Nummer som URI, navn som label
				String stNum = stvalues[0];
				String stName = stvalues[1];
				Individual wSensor = model.createIndividual(Vocab.ns + stNum, vocab.weatherSensorClass);
				Literal nameLabel = model.createLiteral(stName);
				wSensor.addLabel(nameLabel);

				//I drift fra
				String stMonth = stvalues[2].substring(0, 3);
				String stYear = stvalues[2].substring(4);
				String stYearMonth = ParseUtils.fixDateFormat(stMonth, stYear);
				Literal yearMonth = model.createTypedLiteral(stYearMonth, Vocab.xsd + "gYearMonth");
				wSensor.addProperty(vocab.inServiceSince, yearMonth);

				//I drift til (evt. skippe dette feltet, da ingen av stasjonene har noe data der)
				//				wSensor.addProperty(inServiceUntil, stvalues[3]);

				//Høyde over havet
				wSensor.addLiteral(vocab.masl, Integer.parseInt(stvalues[4]));

				//Lokasjon
				wSensor.addProperty(vocab.inPrincipality, stvalues[5]);
				wSensor.addProperty(vocab.inCounty, stvalues[6]);
				wSensor.addProperty(vocab.inRegion, stvalues[7]);	
			}

			//38 linjer med drit
			for (int j = 0; j < 38; j++) {
				reader.readLine();
			}

			String[] headers = reader.readLine().split(";");
			Property[] properties = new Property[headers.length];
			for (int i=0; i<headers.length; i++){
				properties[i] = model.createProperty(model.getNsPrefixURI(Vocab.prefix) + headers[i].toLowerCase());
				//Range for windDirection og wavaCode er nonNegativeInteger, alle andre er float
				if(i > 1){
					if (i == 4 || i == 6) {
						properties[i].addProperty(RDFS.range, XSD.nonNegativeInteger);
					} else
					properties[i].addProperty(RDFS.range, XSD.xfloat);
				}
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				addData(line, model, properties, vocab.weatherMeasurementClass);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} //end parseData


	private static void addData(String line, Model model, Property[] properties, Resource measurementType) {
		Vocab vocab = Vocab.getInstance();
		Resource data = model.createResource();
		data.addProperty(RDF.type, measurementType);
		String[] values = line.split(";");
		//ingen getLiteral()-metode, ser ut som det funker med getResource()
		data.addProperty(vocab.measuredBySensor, model.getResource(Vocab.ns + values[0]));

		String[] rawDate = values[1].split("-");
		String xsdDateString = ParseUtils.dmyToXSDate(rawDate[0]);
		String xsdTimeEndString = rawDate[1];
		String xsdDateTimeEndString = xsdDateString + "T" + xsdTimeEndString;
		String xsdTimeStartString = ParseUtils.calculateStartTime(xsdTimeEndString + ":00");
		String xsdDateTimeStartString = ParseUtils.calculateStartDateTime(xsdDateTimeEndString + ":00");

		Literal xsdDate = model.createTypedLiteral(xsdDateString, Vocab.xsd + "date");
		Literal xsdTimeEnd = model.createTypedLiteral(xsdTimeEndString + ":00", Vocab.xsd + "time");
		Literal xsdTimeStart = model.createTypedLiteral(xsdTimeStartString + ":00", Vocab.xsd + "time");
		Literal xsdDateTimeStart = model.createTypedLiteral(xsdDateTimeStartString + ":00", Vocab.xsd + "dateTime");
		Literal xsdDateTimeEnd = model.createTypedLiteral(xsdDateTimeEndString + ":00", Vocab.xsd + "dateTime");

		Resource instantStart = model.createResource();
		instantStart.addProperty(vocab.inXSDDateTime, xsdDateTimeStart);
		Resource instantEnd = model.createResource();
		instantEnd.addProperty(vocab.inXSDDateTime, xsdDateTimeEnd);

		Resource interval = model.createResource();
		interval.addProperty(vocab.owlBeginning, instantStart);
		interval.addProperty(vocab.owlEnd, instantEnd);

		data.addProperty(vocab.date, xsdDate);
		data.addProperty(vocab.startTime, xsdTimeStart);
		data.addProperty(vocab.measuredTimeInterval, interval);
		data.addProperty(vocab.endTime, xsdTimeEnd);

		data.addProperty(properties[1], xsdDateTimeEnd);

		//Leser ikke inn data der verdien for målingen mangler eller er merket som upålitelig
		//Enkelte measurements blir da stående "blanke", uten mer data enn dato-tid og tilhørende sensor
		for (int i = 2; i < values.length; i++) {
			if (!(values[i].equals(" ")) && (!(values[i].equals("x")))) {
				//Gi verdiene riktig datatype, og sørg for at floats bruker "." og ikke ","
				if (i == 4 || i ==6) {
					Literal value = model.createTypedLiteral(values[i], Vocab.xsd + "nonNegativeInteger");
					data.addLiteral(properties[i], value);
				} else {
				Literal value = model.createTypedLiteral(values[i].replace(",", "."), Vocab.xsd + "float");
				data.addLiteral(properties[i], value);
				}
			}
		}
	} //end addData
}// end class
