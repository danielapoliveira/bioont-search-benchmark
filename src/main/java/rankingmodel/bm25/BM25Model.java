package rankingmodel.bm25;

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



/**
 * Created by Anila Sahar Butt.
 */
public class BM25Model {

	BM25Map diskmap;
	TfIdf_Data tfIdfClass;
	private long AVG_LENGTH = 0;
	private double b = 0.75;
	private double k =2.0;
  	private PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;
	private String path;

	public BM25Model(String path){
		this.path = path;
		diskmap = BM25Map.getDefaultMap(path);
		tfIdfClass= TfIdf_Data.getDefaultMap(path);
		corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
	}
		
 	public ArrayList<ResultFormatter> getRankedClasses(Model model) {

 		BM25Calculator bm25statistics = new BM25Calculator(path);
 		AVG_LENGTH = bm25statistics.calculateAverageDocumentLength();

   		List<String> graphList = new ArrayList<String>();
   		HashMap<String, Double> BM25Scores = new HashMap<String, Double>();
        
        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");
        
        /********************  Get All graphs in the Result Set *****************************/
        
        NodeIterator graphIterator = model.listObjectsOfProperty(graphProperty);
        
        while(graphIterator.hasNext()){
        	String uri = graphIterator.next().toString();
        	graphList.add(uri);
        }
 
        /*********************** Calculate BM25 Value for each graph ***********************************/
        
        for (int i=0; i<graphList.size() ; i++){
        	
        	String graphIRI = graphList.get(i);
        	System.out.println(" /********** "+graphIRI +" *********/");

        	int DOC_LENGTH = diskmap.getDocLength(graphIRI);
        	
        	double score = 0.0;
        	double TF_Value = 0.0;
        	double IDF_Value =0.0;
        	
        	//get all uri's from the graph, and retrieve their tf_Idf scores
        	ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graphIRI));
          
            while(uriIterator.hasNext()){
            	
            	String uri = uriIterator.next().toString();
            	TF_IDFHolder tf_IdfHolder = getTF_IDFValues(uri, graphIRI);
            	TF_Value = tf_IdfHolder.getTF();
            	IDF_Value = tf_IdfHolder.getIDF();
            	
            	score = score + (IDF_Value * ( (TF_Value*(k+1)) / ( TF_Value + k * (1-b + (b *(DOC_LENGTH/AVG_LENGTH))))));	
            }            
            
            BM25Scores.put(graphIRI, score);       	
        }
        
        /*************************** Sort Hash map for bm25 score **********************************/
      
        HashMap<String, Double> sortedBM25Map = sortByValues(BM25Scores);
        
        /************************** put sorted values into  ArrayList *******************************/
        ArrayList<ResultFormatter> resultList = new ArrayList<ResultFormatter>();
       
        for (Map.Entry<String, Double> entry : sortedBM25Map.entrySet()) {
        	
        	String graph = entry.getKey().toString(); 
        	
        	ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graph));

            while(uriIterator.hasNext()){
            	          	
            	ResultFormatter result = new ResultFormatter();
            	String term = uriIterator.next().toString();
            	result.setTermIRI(term);
            	result.setGraphIRI(entry.getKey().toString());
            	result.setTermLabel(getLabel(model.listObjectsOfProperty(model.createResource(term), label), term));
            	result.setScore(entry.getValue().toString());
            	resultList.add(result);
            	
            }
                       
        }
        
        return resultList;
 	}
   		
 	private TF_IDFHolder getTF_IDFValues(String term, String graphIRI){

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

		  	return new TF_IDFHolder(tf, idf, tf_idf);
    }
 	
  	private String getLabel(NodeIterator labelIterator, String propertyURI){
  		String propLabel;
    	
    	if (labelIterator.hasNext()){
    	RDFNode pLabel = labelIterator.nextNode();
    	propLabel = pLabel.toString();
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
	  
	
}
