package rankingmodel.vectorspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import store.QuadStore;

/**
 * Created by Anila Sahar Butt.
 */
public class QueryVector {
    QuadStore store = QuadStore.getDefaultStore();
    VirtGraph connection = store.getConnection();
    double CORPUS_LENGTH = 1032;

    public HashMap<String, Double> getQueryVector(ArrayList<String> queryString){

        HashMap<String, Double> queryVector = getQueryWordsCount(queryString);
        double MAX_FREQUENCY = this.getMaxQueryWordFrequency(queryVector);
        for (Map.Entry<String, Double> entry : queryVector.entrySet()) {
            double frequency = entry.getValue();
            double tf = frequency/MAX_FREQUENCY;
            double df = this.getDF(entry.getKey().toString());
            double idf =  Math.log(CORPUS_LENGTH/df);
            double tfIdf =  tf*idf;
            queryVector.put(entry.getKey().toString(), tfIdf);
        }

        return queryVector;
    }

    public HashMap<String, Double> getQueryWordsCount(ArrayList<String> queryString){
        HashMap<String, Double> queryVector = new HashMap<String, Double>();

        String queryword = "";

        for(int count=0; count<queryString.size() ; count++){
            Double querycount = 0.0;
            queryword = queryString.get(count);
            if (queryVector.containsKey(queryword)){
                querycount = Double.parseDouble(queryVector.get(queryword).toString())+1;
            } else {
                querycount = 1.0;
            }
            queryVector.put(queryword, querycount);

        }
        return queryVector;
    }

    public double getMaxQueryWordFrequency(HashMap<String, Double> queryVector){
        double MAX_FREQUENCY = 0;
        for (Map.Entry entry : queryVector.entrySet()) {
            double frequency = Double.parseDouble(entry.getValue().toString());
            if (frequency > MAX_FREQUENCY)
                MAX_FREQUENCY = frequency;
        }

        return MAX_FREQUENCY;
    }

    public double getDF(String word){
        double DOC_FREQUENCY=0;
        String sparql = " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
                " SELECT count(DISTINCT ?g) as ?count " +
                " Where {GRAPH ?g {" +
                " ?uri rdfs:label ?value." +
                " FILTER regex(?value, \""+ word +"\", \"i\")}}";
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        ResultSet results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = (QuerySolution) results.nextSolution();
            DOC_FREQUENCY = Double.parseDouble(result.getLiteral("count").toString().split("\\^")[0]);
            System.out.println(DOC_FREQUENCY);
        }
        return DOC_FREQUENCY;
    }

    public double getQueryNorm(HashMap<String, Double> queryVector){
        double q=0.0;
        for (Map.Entry entry : queryVector.entrySet()) {
            q = q + Math.pow(Double.parseDouble(entry.getValue().toString()),2);
            System.out.println(q);
        }

        q = Math.sqrt(q);
        System.out.println("Square root of q is :" + q);
        return q;
    }
}
