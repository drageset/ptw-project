package no.uib.info216.ptwp.parsers;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class Vocab {
	
	private static Vocab vocab = null;
	
	OntModel model;
	
	// PREFIXES
	public final static String prefix = "ptw";
	public final static String ns = "http://www.ptwproject.org/ontology#";
	public final static String xsd = "http://www.w3.org/2001/XMLSchema#";
	public final static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	public final static String qb = "http://purl.org/linked-data/cube#";
	public final static String time = "http://www.w3.org/2006/time#";
	public final static String cc = "http://creativecommons.org/ns#";
	
	
	/* PTWP properties */
	Property unitOfMeasurement; // = model.createProperty(ns + "unitOfMeasurement"); //A measurement has a unit of measurement
	Property measuredBySensor; // = model.createProperty(ns + "measuredBySensor"); // each measurement is measured by a sensor
	Property measuredTimeInterval; // = model.createProperty(ns + "measuredTimeInterval"); //some measurements measure a certain time interval
	Property endTime; // = model.createProperty(ns + "endTime");
	Property startTime; // = model.createProperty(ns + "startTime");
	Property valueMeasured; // = model.createProperty(ns + "valueMeasured");
	Property belongsToDataSet; // relation between a datapoint and a dataset
	Property dateTime; // relation between something and it's designated dateTime
	Property measuresRoadLane; // relationship between a traffic sensor and the road lane it measures
	Property roadLaneDirection; // relation between a road lane and it's direction
	
	/* PTWP classes */
	Resource dataSet; // class of data sets
	Resource sensorClass;
	Resource trafficSensorClass;
	Resource measurementClass;
	Resource trafficMeasurementClass;
	Resource feltClass;
	
	/*OWL time props */
	Property owlStartTime; // = model.createProperty(time + "hasStartTime"); // an Interval has a beginning and end, each of which are Instants
	Property owlEndTime; // = model.createProperty(time + "hasEndTime");
	Property inXSDDateTime; // = model.createProperty(time + "inXSDDateTime"); // an Instant can have an XSDDateTime
	
	
	private Vocab(){
		
		model = ModelFactory.createOntologyModel();
		
		/* PREFIXES */
		model.setNsPrefix(Vocab.prefix, ns);
		model.setNsPrefix("ssn", ssn);
		model.setNsPrefix("qb", qb);
		model.setNsPrefix("time", time);
		model.setNsPrefix("cc", cc);
		model.setNsPrefix("xsd", xsd);
		
		/* PTWP classes */
		//sensors
		sensorClass = model.createResource(Vocab.ns + "Sensor");
		sensorClass.addProperty(OWL.sameAs, Vocab.ssn + "Sensor");
		trafficSensorClass = model.createResource(Vocab.ns + "TrafficSensor");
		trafficSensorClass.addProperty(RDFS.subClassOf, sensorClass);
		//measurements
		measurementClass = model.createResource(Vocab.ns + "Measurement");
		measurementClass.addProperty(OWL.sameAs, Vocab.ssn + "Observation");
		trafficMeasurementClass = model.createResource(Vocab.ns + "TrafficMeasurement");
		trafficMeasurementClass.addProperty(RDFS.subClassOf, measurementClass);
		//thingies
		feltClass = model.createResource(Vocab.ns + "RoadLane");
		
		dataSet = model.createResource(Vocab.ns + "DataSet");
		dataSet.addProperty(OWL.sameAs, Vocab.qb + "DataSet");


		
		/* PTWP properties */
		unitOfMeasurement = model.createProperty(ns + "unitOfMeasurement"); //A measurement has a unit of measurement
		measuredBySensor = model.createProperty(ns + "measuredBySensor"); // each measurement is measured by a sensor
		measuredTimeInterval = model.createProperty(ns + "measuredTimeInterval"); //some measurements measure a certain time interval
		endTime = model.createProperty(ns + "endTime");
		startTime = model.createProperty(ns + "startTime");
		valueMeasured = model.createProperty(ns + "valueMeasured");
		belongsToDataSet = model.createProperty(ns + "belongsToDataSet");
		belongsToDataSet.addProperty(OWL.sameAs, qb + "dataSet");
		dateTime = model.createProperty(Vocab.ns + "dateTime");
		dateTime.addProperty(RDFS.range, XSD.dateTime);
		
		measuresRoadLane = model.createProperty(Vocab.ns + "measuresRoadLane");
		measuresRoadLane.addProperty(RDFS.domain, trafficSensorClass);
		roadLaneDirection = model.createProperty(Vocab.ns + "roadLaneDirection");
		roadLaneDirection.addProperty(RDFS.domain, feltClass);
		
		/*OWL time props */
		owlStartTime = model.createProperty(time + "hasStartTime"); // an Interval has a beginning and end, each of which are Instants
		owlEndTime = model.createProperty(time + "hasEndTime");
		inXSDDateTime = model.createProperty(time + "inXSDDateTime"); // an Instant can have an XSDDateTime
	}
	
	public static Vocab getInstance(){
		if(vocab == null){
			vocab = new Vocab();
		}
		return vocab;
	}

}
