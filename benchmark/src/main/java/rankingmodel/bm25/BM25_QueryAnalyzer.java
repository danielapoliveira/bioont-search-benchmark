package rankingmodel.bm25;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;


import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import store.QuadStore;

/**
 * Created by Anila Sahar Butt.
 */
public class BM25_QueryAnalyzer {

    /** Default logger */
    private Logger logger= Logger.getLogger(getClass().getName());
    QuadStore store = QuadStore.getDefaultStore();
    VirtGraph connection = store.getConnection();

    public BM25_QueryAnalyzer(){

    }

    public ArrayList<String> getExistingLoadedOntology(){

        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try {
            com.hp.hpl.jena.query.ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                com.hp.hpl.jena.query.QuerySolution result = results.nextSolution();
                String graph = result.get("graph").toString();
                list.add(graph);
            }
        }   catch(Exception e){

        } finally{
            vqe.close();
        }

        return list;
    }


    public int getTotalOntologyTerms(String ontologyIRI){

        int totalcount=0;

        //logger.info("For ontology " + ontologyIRI);
        String sparql = "SELECT DISTINCT ?term (count(?term) as ?count) "
                + "FROM <"+ontologyIRI+">"
                + "WHERE { { {?term ?p ?o.} UNION {?s ?k ?term} UNION {?t ?term ?q}} "
                + "FILTER (!(isBlank(?term)))"
                + "FILTER (isIRI(?term)) } GROUP BY ?term";
        //logger.info(sparql);

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try{
            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String term = result.get("term").toString();
                int count = result.getLiteral("count").getInt();

                totalcount = totalcount + count;

            }     }   catch(Exception e){

        } finally{
            vqe.close();
        }
        //logger.info(totalcount+"");
        return totalcount;
    }

}
