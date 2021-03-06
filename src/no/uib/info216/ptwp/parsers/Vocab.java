package no.uib.info216.ptwp.parsers;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * The Vocab class is used for storing prefixes, properties and resources that are used
 * across several of the parsers.
 *
 */
public class Vocab {
	
	private static Vocab vocab = null;
	
	OntModel model;
	
	// PREFIXES
	public final static String prefix = "ptw";
	public final static String ns = "http://www.ptwproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";
	public final static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	public final static String time = "http://www.w3.org/2006/time#";
	public final static String cc = "http://creativecommons.org/ns#";
	public final static String prov = "http://www.w3.org/ns/prov#";
	
	
	/* PTWP properties */
	Property unitOfMeasurement; // = model.createProperty(ns + "unitOfMeasurement"); //A measurement has a unit of measurement
	Property measuredBySensor; // = model.createProperty(ns + "measuredBySensor"); // each measurement is measured by a sensor
	Property measuredTimeInterval; // = model.createProperty(ns + "measuredTimeInterval"); //some measurements measure a certain time interval
	Property endTime; // = model.createProperty(ns + "endTime");
	Property startTime; // = model.createProperty(ns + "startTime");
	Property valueMeasured; // = model.createProperty(ns + "valueMeasured");
	Property dateTime; // relation between something and it's designated dateTime
	Property measuresRoadLane; // relationship between a traffic sensor and the road lane it measures
	Property roadLaneDirection; // relation between a road lane and it's direction
	Property inServiceSince; // sensor has been in service since $time
	Property masl; // sensor is located x Meters Above Sea Level
	Property inPrincipality; // sensor is located in "kommune"
	Property inCounty; // sensor is located in "fylke"
	Property inRegion; // sensor is located in region
	Property date; // = model.createProperty(ns + "date");
	
	/* PTWP classes */
	Resource sensorClass;
	Resource trafficSensorClass;
	Resource airSensorClass;
	Resource weatherSensorClass;
	Resource measurementClass;
	Resource airMeasurementClass;
	Resource trafficMeasurementClass;
	Resource weatherMeasurementClass;
	Resource feltClass;
	
	/* Div classes */
	Resource ssnObservation;
	Resource ssnSensor;
	
	/*OWL time props */
	Property owlBeginning; // = model.createProperty(time + "hasBeginning"); // an Interval has a beginning and end, each of which are Instants
	Property owlEnd; // = model.createProperty(time + "hasEnd");
	Property inXSDDateTime; // = model.createProperty(time + "inXSDDateTime"); // an Instant can have an XSDDateTime
	
	
	private Vocab(){
		
		model = ModelFactory.createOntologyModel();
		
		/* PREFIXES */
		model.setNsPrefix(Vocab.prefix, ns);
		model.setNsPrefix("ssn", ssn);
		model.setNsPrefix("time", time);
		model.setNsPrefix("cc", cc);
		model.setNsPrefix("xsd", xsd);
		model.setNsPrefix("prov", prov);
		
		/* Div classes */
		ssnObservation = model.createResource(Vocab.ssn + "Observation");
		ssnSensor = model.createResource(Vocab.ssn + "Sensor");
		
		/* PTWP classes */
		//sensors
		sensorClass = model.createResource(Vocab.ns + "Sensor");
		sensorClass.addProperty(OWL.sameAs, ssnSensor);
		airSensorClass = model.createResource(Vocab.ns + "AirSensor");
		airSensorClass.addProperty(RDFS.subClassOf, sensorClass);
		trafficSensorClass = model.createResource(Vocab.ns + "TrafficSensor");
		trafficSensorClass.addProperty(RDFS.subClassOf, sensorClass);
		weatherSensorClass = model.createResource(Vocab.ns + "WeatherSensor");
		weatherSensorClass.addProperty(RDFS.subClassOf, sensorClass);
		

		//measurements
		measurementClass = model.createResource(Vocab.ns + "Measurement");
		measurementClass.addProperty(OWL.sameAs, ssnObservation);
		airMeasurementClass = model.createResource(Vocab.ns + "AirMeasurement");
		airMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);
		trafficMeasurementClass = model.createResource(Vocab.ns + "TrafficMeasurement");
		trafficMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);
		weatherMeasurementClass = model.createResource(Vocab.ns + "WeatherMeasurement");
		weatherMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);
		
		//thingies
		feltClass = model.createResource(Vocab.ns + "RoadLane");
		
		/* PTWP properties */
		unitOfMeasurement = model.createProperty(ns + "unitOfMeasurement"); //A measurement has a unit of measurement
		measuredBySensor = model.createProperty(ns + "measuredBySensor"); // each measurement is measured by a sensor
		measuredTimeInterval = model.createProperty(ns + "measuredTimeInterval"); //some measurements measure a certain time interval
		endTime = model.createProperty(ns + "endTime");
		startTime = model.createProperty(ns + "startTime");
		valueMeasured = model.createProperty(ns + "valueMeasured");
		dateTime = model.createProperty(Vocab.ns + "dateTime");
		dateTime.addProperty(RDFS.range, XSD.dateTime);
		date = model.createProperty(ns + "date");
		
		measuresRoadLane = model.createProperty(Vocab.ns + "measuresRoadLane");
		measuresRoadLane.addProperty(RDFS.domain, trafficSensorClass);
		roadLaneDirection = model.createProperty(Vocab.ns + "roadLaneDirection");
		roadLaneDirection.addProperty(RDFS.domain, feltClass);
		
		inServiceSince = model.createProperty(Vocab.ns + "inServiceSince");
		inServiceSince.addProperty(RDFS.domain, weatherSensorClass);
		inServiceSince.addProperty(RDFS.range, XSD.gYearMonth);
		masl = model.createProperty(Vocab.ns + "metersAboveSeaLevel");
		masl.addProperty(RDFS.domain, weatherSensorClass);
		masl.addProperty(RDFS.range, XSD.nonNegativeInteger);
		inPrincipality = model.createProperty(Vocab.ns + "inPrincipality");
		inCounty = model.createProperty(Vocab.ns + "inCounty");
		inRegion = model.createProperty(Vocab.ns + "inRegion");
		
		/*OWL time props */
		owlBeginning = model.createProperty(time + "hasBeginning"); // an Interval has a beginning and end, each of which are Instants
		owlEnd = model.createProperty(time + "hasEnd");
		inXSDDateTime = model.createProperty(time + "inXSDDateTime"); // an Instant can have an XSDDateTime
	}
	
	public static Vocab getInstance(){
		if(vocab == null){
			vocab = new Vocab();
		}
		return vocab;
	}
	
	public OntModel getModel(){
		return model;
	}

}
