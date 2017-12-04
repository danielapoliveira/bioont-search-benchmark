package rankingmodel.tf_Idf;

import store.QuadStore;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anila Sahar Butt.
 */
public class TF_IDFQueryAnalyzer {

    /** Default logger */
    //private Logger logger;
    QuadStore store = QuadStore.getDefaultStore();
    VirtGraph connection = store.getConnection();

    public TF_IDFQueryAnalyzer(){

    }

    public ArrayList<String> getExistingLoadedOntology(){

        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try {
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("graph").toString();
                list.add(graph);}
        }   catch(Exception e){

        } finally{
            vqe.close();
        }

        return list;
    }

    public HashMap<String,Integer> getTermCountForOntology(String ontologyIRI){
        HashMap<String,Integer> list = new HashMap<String,Integer>();

        String sparql = "SELECT DISTINCT ?term (count(?term) as ?count) "
                + "FROM <"+ontologyIRI+">"
                + "WHERE { { {?term ?p ?o.} UNION {?s ?k ?term} UNION {?t ?term ?q}} "
                + "FILTER (!(isBlank(?term)))"
                + "FILTER (isIRI(?term)) } GROUP BY ?term";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try{
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String term = result.get("term").toString();
                int count = result.getLiteral("count").getInt();

                list.put(term, count);
            }     }   catch(Exception e){

        } finally{
            vqe.close();
        }

        return list;
    }

    public int getTotalOntologyTerms(String ontologyIRI){

        int count=0;
        String sparql = "SELECT (count(?p) as ?count) "
                + "FROM <"+ontologyIRI+">"
                + "WHERE {?s ?p ?o }";

        //System.out.println(sparql);
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        try{
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                count = result.getLiteral("count").getInt();
            }   }   catch(Exception e){

        } finally{
            vqe.close();
        }
        return count*3;
    }

    public int getTermNumberOfDocContainingTerm(String term){

        int count=0;

        String sparql = "SELECT DISTINCT ?graph "
                + "WHERE {GRAPH ?graph { {{?term ?p ?o.} UNION {?s ?v ?term} UNION {?t ?term ?q}} FILTER (?term = <"+term+">) }}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try{
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("graph").toString();
                count++;
            }
        } catch(Exception e){

        } finally{
            vqe.close();
        }

        return count;
    }

    public int getTermCountPerCorpus(String term){

        int count=0;

        String sparql = "SELECT (count(?term) as ?count) "
                + "WHERE {GRAPH ?graph { {{?term ?p ?o.} UNION {?s ?v ?term} UNION {?t ?term ?q}} FILTER (?term = <"+term+">) }}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try{
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                count = result.getLiteral("count").getInt();
            }     }   catch(Exception e){

        } finally{
            vqe.close();
        }

        return count;
    }
}
