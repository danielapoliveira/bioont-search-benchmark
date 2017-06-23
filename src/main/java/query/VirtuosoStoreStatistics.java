package query;

import java.util.UUID;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;

import store.QuadStore;

/**
 * Created by Anila Sahar Butt.
 */
public class VirtuosoStoreStatistics {

    QuadStore store= QuadStore.getDefaultStore();
    VirtGraph connection= store.getConnection();
    public static int count = -1;

    public int getOntologyCount(){
        int count =0;
        String sparql = "SELECT (count(DISTINCT ?graph) as ?count) WHERE {GRAPH ?graph {?subject ?predicate ?object}}";
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        try {
            ResultSet results = vqe.execSelect();
            QuerySolution result = results.nextSolution();
            count = Integer.parseInt(result.get("count").toString().split("\\^")[0]);
        } catch (Exception exp) {
            System.out.println("Can't process select query because of "+exp);
        } finally {
            vqe.close();
        }

        return count;

    }

    public int getTripleCount(){
        int count =0;

        String sparql = "SELECT (count(?graph) as ?count) WHERE {GRAPH ?graph {?subject ?predicate ?object} FILTER (?graph != <http://browser.csiro.au/metadata>)}";
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        try {
            ResultSet results = vqe.execSelect();
            QuerySolution result = results.nextSolution();
            count = Integer.parseInt(result.get("count").toString().split("\\^")[0]);
        } catch (Exception exp) {
            System.out.println("Can't process select query because of "+exp);
        } finally {
            vqe.close();
        }

        return count;
    }

    public int getClassesCount(){
        int count =0;

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT (count(DISTINCT ?subject) as ?count) "
                + "WHERE {GRAPH ?graph "
                + "{{?subject rdf:type rdfs:Class} "
                + "UNION {?subject rdf:type owl:Class} "
                + "UNION {?subject rdf:type rdf:Class} "
                + "} FILTER (?graph != <http://browser.csiro.au/metadata>)}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        try {
            ResultSet results = vqe.execSelect();
            QuerySolution result = results.nextSolution();
            count = Integer.parseInt(result.get("count").toString().split("\\^")[0]);
        } catch (Exception exp) {
            System.out.println("Can't process select query because of "+exp);
        } finally {
            vqe.close();
        }

        return count;

    }

    public int getPropertiesCount(){
        int count = 0;

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT (count(DISTINCT ?subject) as ?count) "
                + "WHERE {GRAPH ?graph "
                + "{{?subject rdf:type rdfs:Property} "
                + "UNION {?subject rdf:type owl:Property} "
                + "UNION {?subject rdf:type rdf:Property} "
                + "} FILTER (?graph != <http://browser.csiro.au/metadata>)}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        try {
            ResultSet results = vqe.execSelect();
            QuerySolution result = results.nextSolution();
            count = Integer.parseInt(result.get("count").toString().split("\\^")[0]);
        } catch (Exception exp) {
            System.out.println("Can't process select query because of "+exp);
        } finally {
            vqe.close();
        }

        return count;

    }

    public String getKey(){
        UUID idOne = UUID.randomUUID();

        return idOne.toString();

    }

    public static int getCount(){
        count++;
        return count;

    }
    public static void main(String[] args) throws Exception {
        VirtuosoStoreStatistics stat = new VirtuosoStoreStatistics();
        System.out.println("Ontology count: "+stat.getOntologyCount());
        System.out.println("Triple count: "+stat.getTripleCount());
        System.out.println("Classes count: "+stat.getClassesCount());
        System.out.println("Properties count: "+stat.getPropertiesCount());
    }

}
