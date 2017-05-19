package no.uib.info216.ptwp.parsers;

import org.apache.jena.ontology.OntModel;

/**
 * This class writes out the vocabulary in the Vocab class to a turtle file
 *
 */
public class WriteModelToFile {
	public static void main(String[] args) {
		Vocab vocab = Vocab.getInstance();
		OntModel model = vocab.getModel();
		model.write(System.out);
		ParseUtils.writeModelToFile(model, "ontologymodel.ttl", "TURTLE");
	}
}
