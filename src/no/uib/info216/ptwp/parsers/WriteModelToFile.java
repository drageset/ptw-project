package no.uib.info216.ptwp.parsers;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

public class WriteModelToFile {
	public static void main(String[] args) {
		Vocab vocab = Vocab.getInstance();
		OntModel model = vocab.getModel();
		model.write(System.out);
		ParseUtils.writeModelToFile(model, "ontologymodel.ttl", "TURTLE");
	}
}
