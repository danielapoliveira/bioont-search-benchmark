package dataload;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import store.QuadStore;


/**
 * Created by Anila Sahar Butt.
 */
public class MetaDataFactory {

    QuadStore store = QuadStore.getDefaultStore();

    public String owl = "http://www.w3.org/2002/07/owl#";
    public String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public String dcat = "http://www.w3.org/ns/dcat#";
    public String dct = "http://purl.org/dc/terms/";

    Node rdfTypeURI=null, datasetClassURI=null, identifierPropertyURI=null, dateIssued=null, owlImportsURI=null;

    public MetaDataFactory(){
        owlImportsURI = NodeFactory.createURI(owl+"imports");
        rdfTypeURI = NodeFactory.createURI(rdf + "type");
        datasetClassURI = NodeFactory.createURI(dcat+"Dataset");
        identifierPropertyURI = NodeFactory.createURI(dct+"identifier");
        dateIssued = NodeFactory.createURI(dct+"issued");
    }

    public void addMetadata(String uri, List<String> identifier){


        Node datasetURI = NodeFactory.createURI(uri);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        Node currentdate = NodeFactory.createLiteral(dateFormat.format(date));

        List<Triple> triple = new ArrayList<Triple>();
        Triple datasetRecord = new Triple (datasetURI, rdfTypeURI ,datasetClassURI);
        triple.add(datasetRecord);

        for(int count=0; count < identifier.size() ; count++){
            Node identifierNode = NodeFactory.createLiteral(identifier.get(count));
            Triple identifierRecord = new Triple (datasetURI, identifierPropertyURI , identifierNode);

            triple.add(identifierRecord);
        }

        Triple dateOfRecord = new Triple (datasetURI, dateIssued , currentdate );
        triple.add(dateOfRecord);

        store.insertMetaGraphData(triple);
    }

    public void addImportsMetadata(String uri, List<String> importList){

        Triple importRecord;
        Node importOntology;
        Node datasetURI = NodeFactory.createURI(uri);
        List<Triple> triple = new ArrayList<Triple>();

        for (int i=0; i <importList.size() ; i++){
            importOntology = NodeFactory.createURI(importList.get(i));
            importRecord =  new Triple (datasetURI, owlImportsURI, importOntology);
            triple.add(importRecord);
        }
        store.insertMetaGraphData(triple);

    }
}
