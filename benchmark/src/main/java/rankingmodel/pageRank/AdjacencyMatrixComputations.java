package rankingmodel.pageRank;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import store.QuadStore;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class AdjacencyMatrixComputations {

    QuadStore store = QuadStore.getDefaultStore();
    private VirtGraph connection = store.getConnection();
    private Logger logger = Logger.getLogger(getClass().getName());
    private String path;

    public AdjacencyMatrixComputations(String path){
        this.path = path;
    }

    public void createAdjacencyMatrix() {
        DomainAdjacencyMatrix classInstance = DomainAdjacencyMatrix.getDefaultMap(path);

        try {
            ArrayList<String> graphs = this.getExistingLoadedOntology();

            for (int i=0; i<graphs.size(); i++){
                System.out.append("\r"+i*100/graphs.size()+"%").flush();
                String graphIRI = graphs.get(i);
                ArrayList<String> inlinks = this.getInlinks(graphIRI);
                classInstance.save_domain_adjacency_matrix_map(graphIRI, inlinks);
                //logger.info("inlinks :" + inlinks );
                //inlinks.clear();
            }
        }catch(Exception e){
            logger.info(e.toString());
        } finally {
            //classInstance.closeConnection();
        }
    }

    public void createOutLinkMap() {
        OutLinksMap classInstance = OutLinksMap.getDefaultMap(path);
        try {
            ArrayList<String> graphs = this.getExistingLoadedOntology();

            for (int i=0; i<graphs.size(); i++){
                System.out.append("\r"+i*100/graphs.size()+"%").flush();
                String graphIRI = graphs.get(i);
                logger.info("***************** Getting Values for Graph No"+ i +" : Graph URI is :"+graphIRI+"******************");
                int outlinkCount = this.getOutlinksCount(graphIRI);
                classInstance.save_outlinks_map(graphIRI, outlinkCount);
                //logger.info("outlinkCount :" + outlinkCount );
                //inlinks.clear();
            }
        }catch(Exception e){
            logger.info(e.toString());
        } finally {
            //classInstance.closeConnection();
        }
    }

    public ArrayList<String> getExistingLoadedOntology(){

        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        ResultSet results =  vqe.execSelect();
        try{
            while (results.hasNext()) {
                QuerySolution result = (QuerySolution) results.nextSolution();
                String graph = result.get("graph").toString();
                list.add(graph);
            }
        } catch(Exception e){
            logger.info(e +"");
        } finally {
            vqe.close();
        }
        return list;
    }

    public ArrayList<String> getInlinks(String graphIRI){

        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + "FROM <http://browser.csiro.au/metadata> "
                + "WHERE {?graph <http://www.w3.org/2002/07/owl#imports> <"+graphIRI+">}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        ResultSet results = vqe.execSelect();
        try{
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("graph").toString();
                list.add(graph);
            }
        } catch(Exception e){
            logger.info(e +"");
        } finally {
            vqe.close();
        }
        return list;
    }

    public int getOutlinksCount(String graphIRI){

        String sparql = "SELECT (count(DISTINCT ?graph) as ?count) "
                + "FROM <http://browser.csiro.au/metadata> "
                + "WHERE {<"+graphIRI+"> <http://www.w3.org/2002/07/owl#imports> ?graph}";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        ResultSet results = vqe.execSelect();
        int count =0;
        try{
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                count = Integer.parseInt(result.get("count").toString().split("\\^")[0]);
            }
        } catch(Exception e){
            logger.info(e +"");
        } finally {
            vqe.close();
        }
        return count;
    }

}
