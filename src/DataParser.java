import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class DataParser {

	public final static String prefix = "pt";
	public final static String ns = "http://www.PTWproject.org/ontology#";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String filePath = "data.csv";

		String curDir = System.getProperty("user.dir");
		System.out.println("Current sys dir: " + curDir);

		//		Model defModel = ModelFactory.createDefaultModel();
		//		InfModel model = ModelFactory.createRDFSModel(defModel);
		OntModel model = ModelFactory.createOntologyModel();

		model.setNsPrefix(prefix, ns);

		addFile(filePath, model);

		model.write(System.out, "TURTLE");
	}

	private static void addFile (String filePame, OntModel model) throws FileNotFoundException, IOException {
		File csvFile = new File(filePame);

		System.out.println("data filepath: " + csvFile.getAbsolutePath());
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
			final Individual sensor = model.createIndividual( ns + tellepunktPropName, trafficSensorClass);

			Resource feltClass = model.createResource(ns + "RoadLane");
			Property roadLaneDirection = model.createProperty(ns + "roadLaneDirection");
			roadLaneDirection.addProperty(RDFS.domain, feltClass);

			Property measuresRoadLane = model.createProperty(ns + "measuresRoadLane");
			measuresRoadLane.addProperty(RDFS.domain, trafficSensorClass);

			Resource measurementClass = model.createResource(ns + "Measurement");
			Resource trafficMeasurementClass = model.createResource(ns + "TrafficMeasurement");
			trafficMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);

			//			Property date = model.createProperty(ns + "date");
			//			date.addProperty(RDFS.range, XSD.date);

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
			for (int i=0; i<headers.length; i++)
				properties[i] = model.createProperty(model.getNsPrefixURI("pt") + headers[i].toLowerCase());

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

		data.addProperty(properties[0], dmyToXSDate(values[0]));
		data.addProperty(properties[1], values[1] + ":00");
		data.addProperty(model.getProperty(ns + "roadLaneMeasured"), model.getResource(sensor.getURI() + "_" + values[2]));
		for (int i = 3; i < values.length; i++) {
			data.addLiteral(properties[i], Integer.parseInt(values[i]));
		}	
	} //end addData

	private static String dmyToXSDate(String dmy){
		String[] dmyArray = dmy.split(".");
		return dmyArray[2] + "-" + dmyArray[1] + "-" + dmyArray[0];
	}

}
