package rankingmodel.structuralMetrices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import jdbm.PrimaryTreeMap;
import query.ResultFormatter;
import rankingmodel.tf_Idf.TfIdf_Data;


/**
 * Created by Anila Sahar Butt.
 */
public class ClassMatchMeasure {

    private double exactMatchConstant = 0.6;
    private double partialMatchConstant = 0.4;
    private Logger logger = Logger.getLogger(getClass().getName());
    TfIdf_Data tfIdfClass;
    private PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;
    private String path;

    public ClassMatchMeasure(String path){
        this.path = path;
        tfIdfClass= TfIdf_Data.getDefaultMap(path);
        corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
    }


    public ArrayList<ResultFormatter> getRankedClasses(Model model, ArrayList<String> queryString, HashMap<String, Double> classMatchScoreMap) {

        List<String> graphList = new ArrayList<String>();


        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

        /********************  Get All graphs in the Result Set *****************************/

        NodeIterator graphIterator = model.listObjectsOfProperty(graphProperty);

        while(graphIterator.hasNext()){
            String uri = graphIterator.next().toString();
            graphList.add(uri);
            if(!classMatchScoreMap.containsKey(uri))
                classMatchScoreMap.put(uri,0.0);
        }

        /*********************** Calculate cmm Value for each graph ***********************************/

        for (int i=0; i<graphList.size() ; i++){

            String graphIRI = graphList.get(i);

            int exactMatchScore = getExactMatchCount(model, graphIRI, queryString);
            int partialMatchScore = getPartialMatchCount(model, graphIRI, queryString) - exactMatchScore;

            double classMatchScore = ((exactMatchScore * exactMatchConstant) + (partialMatchScore * partialMatchConstant));

            classMatchScoreMap.put(graphIRI, classMatchScore + classMatchScoreMap.get(graphIRI));
        }

        /*************************** Sort Hash map for cmm score **********************************/

        HashMap<String, Double> sortedClassMatchScoreMap = sortByValues(classMatchScoreMap);
        ArrayList<String> _temp = new ArrayList<String>();

        /************************** put sorted values into  ArrayList *******************************/
        ArrayList<ResultFormatter> resultList = new ArrayList<ResultFormatter>();

        for (Map.Entry<String, Double> entry : sortedClassMatchScoreMap.entrySet()) {

            String graph = entry.getKey().toString();

            ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(graph));
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
                }

            }



        }

        return resultList;
    }

    public int getExactMatchCount(Model model, String graphIRI, ArrayList<String> queryString){
        int result = 0;
        //String term="";
        String term = String.join(" ", queryString);
        //if(queryString.size() != 0 )
        //term = queryString.get(0);

        String query="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?uri "
                + "FROM <"+graphIRI+">"
                + "WHERE { "
                + "?uri rdfs:label ?label."
                + "FILTER (lcase(str(?label)) = \""+term.toLowerCase()+"\")}";

        try {
            QueryExecution exec = QueryExecutionFactory.create(query, model);
            ResultSet resultset = exec.execSelect();

            while (resultset.hasNext()){
                resultset.nextSolution();
                result++;

            }
        }catch(Exception e){
            logger.info(query);
            logger.info(e+"");
        }
        return result;
    }

    public int getPartialMatchCount(Model model, String graphIRI, ArrayList<String> queryString){
        int result = 0;
        String term="";
        if(queryString.size() != 0 )
            term = queryString.get(0);

        String query1="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?uri "
                + "FROM <"+graphIRI+">"
                + "WHERE { "
                + "?uri rdfs:label ?label."
                + "FILTER (CONTAINS(lcase(?label), \""+term.toLowerCase()+"\" )";


        String query2 = "";

        for(int i=1; i<queryString.size(); i++){
            query2 = query2 + " || (CONTAINS(lcase(?label), \""+ queryString.get(i).toLowerCase() +"\"))";

        }

        String query3 = ")}";

        String query = query1 + query2 + query3;
        QueryExecution exec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet resultset = exec.execSelect();
            while (resultset.hasNext()){
                resultset.nextSolution();
                result++;
            }
        }catch(Exception e){
            logger.info(e+"");
        }
        return result;
    }

    private String getLabel(NodeIterator labelIterator, String propertyURI){

        String propLabel="";

        if (labelIterator.hasNext()){
            RDFNode pLabel = labelIterator.nextNode();
            propLabel = pLabel.toString();
            //logger.info(propLabel + "is property Label");
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

    public HashMap<String, Double> getClassMatchMeasure(ArrayList<String> graphList, Model model, ArrayList<String> queryString){
        HashMap<String, Double> ClassMatchScore = new HashMap<String, Double>();

        for (int i=0; i<graphList.size() ; i++){

            String graphIRI = graphList.get(i);

            int exactMatchScore = getExactMatchCount(model, graphIRI, queryString);
            int partialMatchScore = getPartialMatchCount(model, graphIRI, queryString) - exactMatchScore;

            double classMatchScore = ((exactMatchScore * exactMatchConstant) + (partialMatchScore * partialMatchConstant));
            ClassMatchScore.put(graphIRI, classMatchScore);
        }
        return ClassMatchScore;
    }
}
