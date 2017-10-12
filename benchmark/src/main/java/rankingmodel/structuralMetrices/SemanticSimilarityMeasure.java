package rankingmodel.structuralMetrices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.*;
import jdbm.PrimaryTreeMap;

import query.ResultFormatter;
import query.TF_IDFHolder;
import rankingmodel.tf_Idf.TfIdf_Data;
import store.QuadStore;


public class SemanticSimilarityMeasure {

	//	Model ontology = ModelFactory.createDefaultModel();
	Paths pathClass;
	private PrimaryTreeMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> map;
	TfIdf_Data tfIdfClass;
	private PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;

	private String path;


	public SemanticSimilarityMeasure(String path){
		this.path = path;
		tfIdfClass = TfIdf_Data.getDefaultMap(path);
		corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
		pathClass = Paths.getDefaultMap(path);
		map = pathClass.get_adjacency_matrix_map();
	}

	public ArrayList<ResultFormatter> getRankedClasses(Model model, ArrayList<String> queryString) {

		ArrayList<ResultFormatter> rf = new ArrayList<ResultFormatter>();

		List<String> graphList = new ArrayList<String>();
		HashMap<String, Double> semanticSimilarityMeasureScore = new HashMap<String, Double>();

		Property props = model.getProperty("http://www.biomeanalytics.com/model/orthoOntology.owl#hasProperty");
		Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
		Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

		/********************  Get All graphs in the Result Set *****************************/

		NodeIterator graphIterator = model.listObjectsOfProperty(graphProperty);

		while(graphIterator.hasNext()){
			String uri = graphIterator.next().toString();
			graphList.add(uri);
		}

		// System.out.println("Graph List : " + graphList);

		/*********************** Calculate ssm Value for each graph ***********************************/

		//   DensityCalculator densityCal = new DensityCalculator();
		//  densityCal.saveDensityforCorpus();

		for (int count=0; count<graphList.size() ; count++){
			//for (int count=0; count<1 ; count++){
			double ssmSumForAllConcepts = 0.0;
			double k =0;
			double SSM=0.0;

			String graphIRI = graphList.get(count);

			//String graphIRI = "http://www.berkeleybop.org/ontologies/upheno.owl";
			//System.out.println(" /********** "+graphIRI +" *********/");
			//ontology.removeAll();
			//Model ontology = ModelFactory.createDefaultModel();

			//get All matched resources(classes) that belongs to this graph and store them in a list

			ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graphIRI));
			ArrayList<String> resources = new ArrayList<String>();

			while(uriIterator.hasNext()){
				String uri = uriIterator.next().toString();
				resources.add(uri);
			}

			k = resources.size();
			//System.out.println(" Value of K : " + k);

			//for each graph get the rdfs:subClassOf relationships and store them in a model
			//ontology = this.getOntologyGraph(graphIRI);

			// for each combination of matched resuorces calculate ssm and add it to the total ssm for this graph
			for(int i=0; i<resources.size() ; i++){

				for (int j=i+1; j<resources.size() ; j++){

					//get ssm for a pair of resources
					double ssmForConcepts = 1/this.getMinimumPathLength(resources.get(i), resources.get(j), graphIRI);

					//System.out.println( "ssm ("+resources.get(i)+","+resources.get(j)+")  : " +  ssmForConcepts);
					ssmSumForAllConcepts = ssmSumForAllConcepts + ssmForConcepts;
				}
			}
			//System.out.println( "ssmSumForAllConcepts   : " +  ssmSumForAllConcepts);

			//get final normalized ssm for this graph
			SSM = (1/k) * ssmSumForAllConcepts;
			// System.out.println( "SSM ("+graphIRI+")   : " +  SSM);

			// add uri of this graph along with its ssm into the hashmap
			semanticSimilarityMeasureScore.put(graphIRI, SSM);
		}

		/*************************** Sort Hash map for ssm score **********************************/

		HashMap<String, Double> sortedSemanticSimilarityMeasureScore = sortByValues(semanticSimilarityMeasureScore);

		/************************** put sorted values into  ArrayList *******************************/
		ArrayList<ResultFormatter> resultList = new ArrayList<ResultFormatter>();
		ArrayList<String> _temp = new ArrayList<String>();

		for (Map.Entry<String, Double> entry : sortedSemanticSimilarityMeasureScore.entrySet()) {

			String graph = entry.getKey().toString();

			ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graph));

//        	HashMap<String, Double> sorted = new HashMap<String, Double>();
//        	HashMap<String, Double> map = new HashMap<String, Double>();
//        	
//        	while(uriIterator.hasNext()){
//        		String term = uriIterator.next().toString();
//        		if(_temp.contains(term)) { }
//            	else {
//            	    _temp.add(term);
//            	    TF_IDFHolder holder = new TF_IDFHolder();
//            	    holder = getTF_IDFValues(term, graph);
//            	    double tfIdf = holder.getTF_IDF();
//            	    map.put(term, tfIdf);   
//            	    }     		
//        	}
//        	
//        	sorted = this.sortByValues(map);
//        	
//        	for (Map.Entry<String, Double> entry2 : sorted.entrySet()) { 
//        		ResultFormatter result = new ResultFormatter();
//        		String term = entry2.getKey();
//            	result.setTermIRI(term);
//            	result.setGraphIRI(graph);
//            	result.setTermLabel(getLabel(model.listObjectsOfProperty(model.createResource(term), label), term));
//            	result.setScore(entry.getValue().toString());
//            	resultList.add(result);
//            	System.out.println("" + term + "" + entry.getValue());
//        	}
//        	
//        }
//        	ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(entry.getKey().toString()));
			// HashMap<String, Double> resourceMap = new HashMap<String, Double>();
			while(uriIterator.hasNext()){
				ResultFormatter result = new ResultFormatter();
				String term = uriIterator.next().toString();
				if(_temp.contains(term)) { }
				else {
					_temp.add(term);

					result.setTermIRI(term);
					result.setGraphIRI(entry.getKey().toString());
					result.setTermLabel(getLabel(model.listObjectsOfProperty(model.createResource(term), label), term));
					result.setScore(entry.getValue().toString());
					resultList.add(result);
					//System.out.println(" Term " + term + " Score " + entry.getValue().toString() );
					//System.out.println(term);
				}

			}
		}
		return resultList;
	}

	private TF_IDFHolder getTF_IDFValues(String term, String graphIRI){
		//System.out.println("TERMS " + term +" GRAPHS " + graphIRI );
		TF_IDFHolder tf_IdfHolder = new TF_IDFHolder();

		double tf = 0;
		double idf = 0;
		double tf_idf = 0;

		if (corpus_tfIdf_Map.containsKey(graphIRI)) {
			HashMap<String, HashMap<Integer,Double>> ontologyTfIDFs = corpus_tfIdf_Map.get(graphIRI);
			if(ontologyTfIDFs.containsKey(term)) {
				HashMap<Integer,Double> tfIdfs = ontologyTfIDFs.get(term);
				tf = tfIdfs.get(1);
				idf = tfIdfs.get(2);
				tf_idf = tfIdfs.get(3);
			}
		}

		tf_IdfHolder = new TF_IDFHolder(tf, idf, tf_idf);
		return tf_IdfHolder;
	}

	private String getLabel(NodeIterator labelIterator, String propertyURI){

		String propLabel="";

		if (labelIterator.hasNext()){
			RDFNode pLabel = labelIterator.nextNode();
			propLabel = pLabel.toString();
			//System.out.println(propLabel + "is property Label");
			if(propLabel.contains("@")) {
				propLabel=propLabel.split("@")[0];
			}
			if (propLabel.contains("^")){
				propLabel= propLabel.split("\\^")[0];
			}
		} else {
			propLabel = propertyURI;
		}
		return propLabel;
	}

	public double getMinimumPathLength(String node1, String node2, String graphIRI){
		double count = 20;

		try {
			if(map.containsKey(graphIRI)){

				HashMap<String, HashMap<String, ArrayList<String>>> ontologyMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
				ontologyMap = map.get(graphIRI);

				for (Map.Entry<String, HashMap<String, ArrayList<String>>> entry2: ontologyMap.entrySet()){
					String firstNode = entry2.getKey().toString();
					if ((firstNode.equalsIgnoreCase(node1)) || (firstNode.equalsIgnoreCase(node2)) )  {
						HashMap<String, ArrayList<String>> classMap = new HashMap<String, ArrayList<String>>();
						classMap = entry2.getValue();
						for (Map.Entry<String, ArrayList<String>> entry3: classMap.entrySet()){
							String secondNode = entry3.getKey().toString();
							if ( (secondNode.equalsIgnoreCase(node1)) || (secondNode.equalsIgnoreCase(node2)))  {
								ArrayList<String> pathNodes = entry3.getValue();
								count = pathNodes.size();
							} else {

							}
						}
					} else {

					}
				}
			}
		}catch (Exception e) {

		} finally {
				//pathClass.closeConnection();
		}
		return count;
	}

	public ArrayList<String> getSuperClassListForList(ArrayList<String> list, String graphIRI){

		ArrayList<String> superClassList = new ArrayList<String>();

		for (int i=0; i<list.size() ; i++){
			ArrayList<String> newList = new ArrayList<String>();
			newList = this.getSuperClassList(list.get(i), graphIRI);
			for(int j=0; j<newList.size(); j++){
				superClassList.add(newList.get(j));
			}
		}

		return superClassList;
	}

	public ArrayList<String> getSuperClassList(String concept, String graphIRI){
		//int length = 0 ;

		ArrayList<String> superClassList = new ArrayList<String>();

		//System.out.println("**************** SuperClass for concept " + concept + " **************** ");

		String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
				"CONSTRUCT {<"+concept+"> rdfs:subClassOf ?object}"
				+ " FROM <"+graphIRI+"> "
				+ " WHERE {<"+concept+"> rdfs:subClassOf ?object."
				+ "{?object rdf:type rdfs:Class} UNION {?object rdf:type owl:Class.}"
				+ "FILTER (!(isBlank(?object)))}";

		//System.out.println(sparql);

		try {
			QuadStore store = QuadStore.getDefaultStore();
			Model model = store.execConstruct(sparql, false);
			Property subClassOf = model.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
			NodeIterator nIt = model.listObjectsOfProperty(subClassOf);

			while (nIt.hasNext()){
				RDFNode rs = nIt.nextNode();
				//System.out.println(rs.toString());
				superClassList.add(rs.toString());
			}
		} catch(Exception e){
			System.out.println(e);
		}


//		String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
//  				+ " SELECT DISTINCT ?superClasses WHERE {<"+concept+"> rdfs:subClassOf ?superClasses}";
//  		System.out.println(query);
//  		try {
//	 		  QueryExecution exec = QueryExecutionFactory.create(query, graphIRI);
//	    	  ResultSet resultset = exec.execSelect();	  
//	    	  while (resultset.hasNext()){
//	    		  QuerySolution qs = resultset.nextSolution();
//	    		  String superClass = qs.getResource("superClasses").toString();
//	    		  System.out.println(superClass);
//	    		  superClassList.add(superClass);
//	    	  }
//		 }catch(Exception e){
//			// System.out.println(query);
//			 System.out.println(e);
//		 }		

		return superClassList;
	}

//  	public Model getOntologyGraph(String ontologyIRI){
//  		String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
//  				"CONSTRUCT {?subject rdfs:subClassOf ?object} FROM <"+ontologyIRI+"> WHERE {?subject rdfs:subClassOf ?object}";
//  		System.out.println(sparql);
//  		QuadStore store = QuadStore.getDefaultStore();
//		Model model = store.execConstruct(sparql, false);
//		//model.toString();
//  		return model;
//  	}

	public double intersection(HashMap<String,Double> map1, HashMap<String, Double> map2) {
		double length = 0.0;


		for(Map.Entry<String, Double> entry : map1.entrySet()){
			if(map2.containsKey(entry.getKey())){
				length = map1.get(entry.getKey()) + map2.get(entry.getKey());
				break;
			}
		}

		return length;
	}

	public HashMap<String, Double> copyMap(HashMap<String, Double> map, ArrayList<String> list, double length) {
		//HashMap<String, Double> map = new HashMap<String, Double>();

		for(int i=0; i<list.size();i++){
			if (map.containsKey(list.get(i))){
			} else {
				map.put(list.get(i), length);}
		}
		return map;
	}

	private HashMap<String, Double> sortByValues(HashMap<String, Double> map) {
		List list = new ArrayList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey().toString(), Double.parseDouble(entry.getValue().toString()));
		}
		return sortedHashMap;
	}


	public HashMap<String, Double> getsemanticSimilarityMeasureScore(ArrayList<String> graphList, Model model){
		HashMap<String, Double> semanticSimilarityMeasureScore = new HashMap<String, Double>();
		//   DensityCalculator densityCal = new DensityCalculator();
		//  densityCal.saveDensityforCorpus();
		Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

		for (int count=0; count<graphList.size() ; count++){
			//for (int count=0; count<1 ; count++){
			double ssmSumForAllConcepts = 0.0;
			double k =0;
			double SSM=0.0;

			String graphIRI = graphList.get(count);

			//String graphIRI = "http://www.berkeleybop.org/ontologies/upheno.owl";
			//System.out.println(" /********** "+graphIRI +" *********/");
			//ontology.removeAll();
			//Model ontology = ModelFactory.createDefaultModel();

			//get All matched resources(classes) that belongs to this graph and store them in a list

			ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graphIRI));
			ArrayList<String> resources = new ArrayList<String>();

			while(uriIterator.hasNext()){
				String uri = uriIterator.next().toString();
				resources.add(uri);
			}

			k = resources.size();
			//System.out.println(" Value of K : " + k);

			//for each graph get the rdfs:subClassOf relationships and store them in a model
			//ontology = this.getOntologyGraph(graphIRI);

			// for each combination of matched resuorces calculate ssm and add it to the total ssm for this graph
			for(int i=0; i<resources.size() ; i++){

				for (int j=i+1; j<resources.size() ; j++){

					//get ssm for a pair of resources
					double ssmForConcepts = 1/this.getMinimumPathLength(resources.get(i), resources.get(j), graphIRI);

					//System.out.println( "ssm ("+resources.get(i)+","+resources.get(j)+")  : " +  ssmForConcepts);
					ssmSumForAllConcepts = ssmSumForAllConcepts + ssmForConcepts;
				}
			}
			//System.out.println( "ssmSumForAllConcepts   : " +  ssmSumForAllConcepts);

			//get final normalized ssm for this graph
			SSM = (1/k) * ssmSumForAllConcepts;
			//System.out.println( "SSM ("+graphIRI+")   : " +  SSM);

			// add uri of this graph along with its ssm into the hashmap
			semanticSimilarityMeasureScore.put(graphIRI, SSM);
		}
		return semanticSimilarityMeasureScore;
	}
}
