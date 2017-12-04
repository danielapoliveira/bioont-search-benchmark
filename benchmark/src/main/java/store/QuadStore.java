/*
 * Copyright (c) 2014, CSIRO and/or its constituents or affiliates. All rights reserved.
 * Use is subject to license terms.
 */

package store;


import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import virtuoso.jena.driver.*;
import test.Configuration;


/**
 * RDF triple store interface for the API. Services can't instantiate this store, instead they
 * should use {@link #getDefaultStore} method to get its singleton.
 *
 * @author anila butt
 */
public class QuadStore {

    /** Default instance of the triple store */
    private static QuadStore defaultStore;

    /** Default logger */
    private Logger logger;

    /** Sesame repository for the quad store*/
    private VirtGraph  metagraph = null;

    private VirtGraph connection = null;

    private String connectionURL = null;

    private String username = null;

    private String password = null;

    /** Sesame Repository connection */
    //private RepositoryConnection connection= null;
    /**
     * Gets default triple store instance to avoid synchronization faults
     */
    public static QuadStore getDefaultStore() {
        if(defaultStore==null) {
            defaultStore = new QuadStore();
        }
        return defaultStore;
    }

    /**
     * Initializes this triple store
     */
    private QuadStore() {
        logger = Logger.getLogger(getClass().getName());

        connectionURL = "jdbc:virtuoso://" + Configuration.getProperty(Configuration.VIRTUOSO_INSTANCE) + ":" + Configuration.getProperty(Configuration.VIRTUOSO_PORT);
        logger.info("Connection URL :" + connectionURL);

        username = Configuration.getProperty(Configuration.VIRTUOSO_USERNAME);
        logger.info("VIRTUOSO USER :" + username);

        password = Configuration.getProperty(Configuration.VIRTUOSO_PASSWORD);
        logger.info("VIRTUOSO PASSWORD :" + password);

        String metadata = Configuration.getProperty(Configuration.STORE_METADATA);
        logger.info("VIRTUOSO CONTEXT PATH :" + metadata);

        //VirtGraph graph = new VirtGraph ("Example2", "jdbc:virtuoso://localhost:1111", "dba", "dba");

        //graph = new VirtGraph(context, connectionURL, username , password );

        metagraph = new VirtGraph(metadata, connectionURL, username , password );
        connection = new VirtGraph(connectionURL, username , password );

        logger.info("Connection established . . . ");


    }

    public VirtGraph getConnection(){

        return connection;
    }

    public void insertMetaGraphData(List<Triple> triple) {
        //logger.info("Loading data from : " + strurl);
        //try {

        GraphUtil.add(metagraph,triple);
        //metagraph.getBulkUpdateHandler().add(triple);
        //logger.info("Triple loaded successfully");
        //} catch (Exception e) {
        //	logger.severe("Error[" + e + "]");
        //}
        //logger.info("TDB triple store initialized");
    }


    public boolean loadGraph(String uri){
        boolean flag = false;
        String loadString  = "LOAD <"+uri+">  INTO <"+uri+"> ";
        try {
            VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(loadString, connection);
            vur.exec();
            flag = true;
            //logger.info("File loaded successfully");
        } catch (Exception e){


        }
        return flag;
    }


    /**
     * Inserts RDF data into virtuoso QUAD store
     *
     * @param filepath Input file or directory path
     * @return Number of triples inserted into this triple store
     */
    public void insert(File filepath) {
        logger.info("Loading data from file: " + filepath);

        //Model model = new TDBModel();
//		boolean directory = filepath.isDirectory();
//		
//		if(directory) {	 
//			File[] files = filepath.listFiles();
//	        for(File file:files) {
//	            try {
//	                logger.info(file.getAbsolutePath());	    			
//	    			graph.read(file.getAbsolutePath(), OntMediaType.MIME_RDF_XML);
//	                logger.info("File loaded successfully");
//	            } catch(Exception e) {
//	                logger.info("Cant load file: "+e);
//	            }
//	        }
//		} else {
//			try {
//				logger.info(filepath.getAbsolutePath());	    			
//				graph.read(filepath.getAbsolutePath(), OntMediaType.MIME_RDF_XML);
//	            logger.info("File loaded successfully");			
//			} catch (Exception e) {
//				logger.severe("Error[" + e + "]");
//			}
//		}
//        logger.info("TDB triple store initialized");
    }

    /**
     * Inserts RDF data into virtuoso QUAD store from a url
     *
     * @param strurl Input file or directory path
     * @return Number of triples inserted into this triple store
     */
    public boolean insert(String strurl) {
        VirtGraph _graph = new VirtGraph(strurl, connectionURL, username , password );

        logger.info("Loading data from : " + strurl);
        boolean flag = false;
        try {

            _graph.read(new URL(strurl).toString(), OntMediaType.MIME_RDF_XML);
            //graph.read(strurl, OntMediaType.MIME_RDF_XML);
            flag = true;
            if(flag == true) {

            } else {

            }
            logger.info("File loaded successfully");
        } catch (Exception e) {
            flag = false;
            logger.severe("Error[" + e + "]");
        } finally{
            _graph.close();
        }
        logger.info("TDB triple store initialized");
        return flag;
    }

    /**
     * Executes given Graph type query
     *
     * @param query The SPARQL construct or describe query
     * @return The resulting graph as a string
     */
    public Model execConstruct(String query, boolean inf) {

//        Model queryModel = TDBFactory.createModel();
//        if(inf==true){
//        	ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, orthoModel);
//        	queryModel = ontModel;
//        }else{queryModel = orthoModel; }

        //Query sparql = QueryFactory.create(query);
        //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, connection);

        Model _model = null;
        logger.info("Query parsed successfully");
        try {

            _model = (Model) vqe.execConstruct();

        } catch (Exception exp) {
            logger.info("Can't process describe query because of "+exp);
        } finally {
            vqe.close();
        }
        return _model;
    }

    public ResultSet execSelect(String query, boolean inf) {

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, connection);

        logger.info("Query parsed successfully");

        ResultSet results =null;

        try {
            results =  vqe.execSelect();
            //logger.info("Result Set contains :" + results.getResultVars().toString() + results.getRowNumber());
        } catch (Exception exp) {
            logger.info("Can't process select query because of "+exp);
        } finally {
            vqe.close();
        }
        return results;
    }

//	public GraphQueryResult execGraphQuery(String query) throws RepositoryException, MalformedQueryException {
//		
//		GraphQuery gQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, query);
//		logger.info("Parsed query successfully");
//		
//		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
//		RDFWriter writer = Rio.createWriter(RDFFormat.N3, byteout);
//		
//		//Graph graph = new GraphImpl();
//		String str = "";
//		GraphQueryResult qResult= null;
//		try {
//			qResult = gQuery.evaluate();
//			logger.info("Construct query returned  triples");
////		//	writer.startRDF();
////
////			for (int row = 0; qResult.hasNext(); row++) {
////				Statement pairs = qResult.next();
////				//writer.handleStatement(pairs);
////				model.add(pairs);
////			}
//			//writer.endRDF();
//			//str = byteout.toString();
//		}catch (Exception exp) {
//			logger.info("Can't process construct query because of "+exp);
//		} finally {
//			gQuery.clearBindings();
//		}
//		return qResult;
//	}

    /**
     * Executes given Tuple type query
     *
     * @param query The SPARQL Select query
     * @return The resulting tuples as a string
     * @throws MalformedQueryException
     * @throws RepositoryException
     */


//	public String execTupleQuery(String query) throws RepositoryException, MalformedQueryException {
//		
//		TupleQuery tQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
//		logger.info("Parsed query successfully");
//		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
//		SPARQLResultsXMLWriter writer = new SPARQLResultsXMLWriter(byteout);
//		//RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML, byteout);
//		String str = "";
//		try {
//			tQuery.evaluate(writer);
//			logger.info("Tuple query returned triples");			
//			str = byteout.toString();
//		}catch (Exception exp) {
//			logger.info("Can't process construct query because of "+exp);
//		} finally {
//			tQuery.clearBindings();
//		}
//		return str;
//	}

    /**
     * Executes given boolean type query
     *
     * @return The resulting boolean
     */

//	public boolean execBooleanQuery(String query) throws RepositoryException, MalformedQueryException {
//		
//		BooleanQuery bQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
//		logger.info("Parsed query successfully");
//
//		boolean str = false;
//		try {
//			str = bQuery.evaluate();
//			logger.info("Tuple query returned triples");
//		}catch (Exception exp) {
//			logger.info("Can't process construct query because of "+exp);
//		} finally {
//			bQuery.clearBindings();
//		}
//		return str;
//	}
    private static File[] getFileList(String dirPath) {
        File dir = new File(dirPath);

        return dir.listFiles();
    }
    public ArrayList<String> getExistingLoadedOntology(){

        //System.out.println("  *  * * * * GET EXISTING ONTOLOGIES * * * * * * ");
        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try {
            ResultSet results =  vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("graph").toString();
                list.add(graph);
            }
        }   catch(Exception e){

        } finally{
            vqe.close();
        }

        return list;
    }

    public static void main(String[] args) throws IOException {
		/*String sparql = "DROP SILENT ALL";
		QuadStore store = QuadStore.getDefaultStore();
		VirtGraph connection = store.getConnection();

		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
		vqe.execSelect();*/

        QuadStore qd = new QuadStore();
        ArrayList<String> store = qd.getExistingLoadedOntology();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("filename.txt"))));
        String line;
        //Read File Line By Line
        while ((line = br.readLine()) != null)   {
            if(!store.contains(line))
                qd.insert(line);
        }


    }


}