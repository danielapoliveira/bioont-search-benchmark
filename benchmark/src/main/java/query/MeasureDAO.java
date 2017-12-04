package query;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import core.LoggerService;
import store.QuadStore;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anila Sahar Butt.
 */
public class MeasureDAO extends LoggerService {

	/**
	 * Default constructor to initializes logging by its parent.
	 */
	public MeasureDAO() {
		super(MeasureDAO.class.getName());
	}

	public Model getSearchResults(ArrayList<String> searchStringArray) {
		Model result = ModelFactory.createDefaultModel();
		System.out.println(searchStringArray);
		try {

			String sparql = getSearchQuery(searchStringArray);
			QuadStore store = QuadStore.getDefaultStore();
			result = store.execConstruct(sparql, true);

		} catch(Exception exp) {
			logger.info("Exception in getSearchResults "+exp);
		}
		return result;
	}


	public String getSearchQuery(ArrayList<String> q){

		String firstWord = "";
		if(q.isEmpty())
			System.out.println("querywords number is  : "+q.size());
		else firstWord = q.get(0);

		String first = "PREFIX ortho:<http://www.biomeanalytics.com/model/orthoOntology.owl#>" +
				" PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
				" PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
				" PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				" CONSTRUCT {?uri rdfs:graph ?g.?uri rdfs:label ?value}" +
				" Where {GRAPH ?g {" +
				" ?uri rdfs:label ?value." +
				" { {?uri rdf:type rdfs:Class.} UNION { ?uri rdf:type owl:Class.} }" +
				" FILTER (regex(?value, \""+ firstWord +"\", \"i\")";


		String middle  = "";
		for(int i=1; i<q.size(); i++ ){
			middle = middle + " || regex(?value, \""+ q.get(i) +"\", \"i\")";
		}

		String last =  " )}}" ;

		String sparql = first+middle+last;
		//logger.info("GET query prepared");
		//logger.info(sparql);
		return sparql;
	}

	public HashMap<String,String> getLabelComment(String uri){
		HashMap<String,String> descrption = new HashMap<String,String>();
		String sparql ="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"CONSTRUCT { <"+uri+"> rdfs:label ?label."
				+ "<"+uri+"> rdfs:comment ?comment.}"
				+ "WHERE {GRAPH ?g {<"+uri+"> rdfs:label ?label."
				+ "OPTIONAL {<"+uri+"> rdfs:comment ?comment.}}}" ;

		System.out.println(sparql);
		try{
			QuadStore store = QuadStore.getDefaultStore();
			Model model = store.execConstruct(sparql, false);

			Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
			Property comment = model.getProperty("http://www.w3.org/2000/01/rdf-schema#comment");

			NodeIterator nIt = model.listObjectsOfProperty(label);

			String classLabel = "";
			while(nIt.hasNext()){
				classLabel = nIt.nextNode().toString();
			}

			NodeIterator nIt2 = model.listObjectsOfProperty(comment);

			String classComments = "";
			while(nIt2.hasNext()){
				classComments = nIt2.nextNode().toString();
			}

			descrption.put("label", classLabel);
			descrption.put("comment", classComments);


		}catch(Exception e){
			System.out.println(e);
		}

		return descrption;
	}
}
