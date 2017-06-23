package dataload;

import java.util.ArrayList;
import java.util.logging.Logger;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import store.QuadStore;

/**
 * Created by Anila Sahar Butt.
 */
public class ImportsFinder {

	public QuadStore store = QuadStore.getDefaultStore();
	private Logger logger;

	public ArrayList<String> getImportOntologies(String graph){

		ArrayList<String> existingOntologiesList = getExistingLoadedOntology();
		ArrayList<String> ontologiesList = new ArrayList<>();

		String queryString = "PREFIX owl:<http://www.w3.org/2002/07/owl#> "+
				"CONSTRUCT {?uri owl:imports ?ontology} FROM <"+graph+"> WHERE {?uri owl:imports ?ontology. FILTER ((?ontology != <"+graph+">) && (?uri != ?ontology))}";

		System.out.println(queryString);

		Model result = store.execConstruct(queryString, false);
		StmtIterator iter = result.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.nextStatement();
			String ontology = stmt.getObject().toString();
			System.out.println(" Ontology : " + ontology);
			if(existingOntologiesList.contains(ontology)){

			} else {
				ontologiesList.add(ontology);
			}
		}

		return ontologiesList;
	}

	private ArrayList<String> getExistingLoadedOntology(){
		ArrayList<String> list = new ArrayList<>();

		String queryString = "PREFIX owl:<http://www.w3.org/2002/07/owl#> "+
				"CONSTRUCT {?graph owl:subClassOf owl:Class} "
				+ "WHERE {GRAPH ?graph {?subject ?property ?object}}";

		Model result = store.execConstruct(queryString, false);
		StmtIterator iter = result.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.nextStatement();
			String ontology = stmt.getSubject().toString();
			list.add(ontology);
		}

		return list;
	}
}
