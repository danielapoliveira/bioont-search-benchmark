package rankingmodel.pageRank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import rankingmodel.tf_Idf.TfIdf_Data;
import store.QuadStore;

import jdbm.PrimaryTreeMap;

/**
 * Created by Anila Sahar Butt.
 */
public class PageRankTF_IDFMap {
	

	QuadStore store = QuadStore.getDefaultStore();
	private VirtGraph connection = store.getConnection();
	private Logger logger = Logger.getLogger(getClass().getName());
	
	PageRankMap classInstance;
	
	TfIdf_Data tfIdfClass;
	PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;

	private String path;

	public PageRankTF_IDFMap(String path){
		this.path = path;
		classInstance =  PageRankMap.getDefaultMap(path);
		tfIdfClass = TfIdf_Data.getDefaultMap(path);
		corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();

	}
	public void initializePageRankScore() {

 		PrimaryTreeMap<String, Double> tf_Idf_map = classInstance.get_tf_Idf_map();
 		
 		/************************************************************************/
 		ArrayList<String> graphs = this.getExistingLoadedOntology();
 		
 		for (int i=0; i<graphs.size(); i++) {
			System.out.append("\rPR: "+i*100/graphs.size()+"%").flush();
			String graphIRI = graphs.get(i);
			if (graphIRI.endsWith(".owl")) {
				logger.info("***************** Getting Values for Graph No" + i + " : Graph URI is :" + graphIRI + "******************");

				ArrayList<String> classList = this.getClassList(graphIRI);

				for (int j = 0; j < classList.size(); j++) {
					String classIRI = classList.get(j);
					double tf_Idf = this.getTF_IDFValues(classIRI, graphIRI);
					if (tf_Idf_map.containsKey(classIRI)) {
						tf_Idf = tf_Idf + tf_Idf_map.get(classIRI);
						classInstance.save_tf_Idf_map(classIRI, tf_Idf);
					}
					else {
						classInstance.save_tf_Idf_map(classIRI, tf_Idf);
					}
				}
			}
		}
 		//classInstance.closeConnection();
 	  }
	
	
	public void initializePageRankScoreForOntologyGraphs() {
		
 		/************************************************************************/
 		ArrayList<String> graphs = this.getExistingLoadedOntology();
 		double size = graphs.size();
 		double initscore = 1/size;
 		for (int i=0; i<size; i++){
            System.out.append("\rPR2: "+i*100/size+"%").flush();
 			String graphIRI = graphs.get(i);
 				logger.info("***************** Getting Values for Graph No"+ i +" : Graph URI is :"+graphIRI+"initscore : " +initscore+"******************");	
 				classInstance.save_tf_Idf_map(graphIRI, initscore);
 				}
 		//classInstance.closeConnection();
 	  }
	
	public ArrayList<String> getClassList(String graphIRI){

  		ArrayList<String> classList = new ArrayList<String>();
  		
  		//logger.info("**************** SuperClass for concept " + concept + " **************** ");
  		
  		String sparql = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
  				+ "SELECT DISTINCT ?subject "
  				+ "FROM <"+graphIRI+"> "
  				+ "WHERE{{{?subject rdf:type rdfs:Class} UNION {?subject rdf:type owl:Class.}} "
  				+ "FILTER (!isBlank(?subject))}";

			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
			
			try {
		    	  ResultSet results = (ResultSet) vqe.execSelect();
		    	  while (results.hasNext()){
		    		  QuerySolution qs = results.nextSolution();
		    		  String uri = qs.getResource("subject").toString();
		    		  //logger.info(uri);
		    		  classList.add(uri);
		    	  }
			} catch(Exception e){
				logger.info(e.toString());
			}
			//logger.info(classList.toString());
		return classList;
	}
	
	public ArrayList<String> getExistingLoadedOntology(){
		ArrayList<String> list = new ArrayList<String>();
		 
		String sparql = "SELECT DISTINCT ?graph "
				+ " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";
		 
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

			ResultSet results = vqe.execSelect();
			while (results.hasNext()) {
				QuerySolution result = results.nextSolution();
			    String graph = result.get("graph").toString();
			    list.add(graph);
			}     
		    
			return list;	         
	 }
	
 	private Double getTF_IDFValues(String term, String graphIRI){
 		double tf_Idf = 0.0;
 		if (corpus_tfIdf_Map.containsKey(graphIRI)){
 			try{
 	 			HashMap<String, HashMap<Integer,Double>> tf_Idfs = corpus_tfIdf_Map.get(graphIRI);
 				if(tf_Idfs.containsKey(term)) {
 					HashMap<Integer,Double> tf_Idf_map = tf_Idfs.get(term);
 					tf_Idf = tf_Idf_map.get(3);
 				}

 			} catch(Exception e) {
 				logger.info("can not calculate  doc norm because :" + e +term);
 			}finally {

 			}
 		}  
 	      return tf_Idf;
 	}

}
