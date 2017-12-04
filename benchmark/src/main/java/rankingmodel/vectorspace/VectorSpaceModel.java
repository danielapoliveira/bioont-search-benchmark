package rankingmodel.vectorspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import query.ResultFormatter;
import query.TF_IDFHolder;
import rankingmodel.tf_Idf.TfIdf_Data;


/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class VectorSpaceModel {

    Logger logger = Logger.getLogger(getClass().getName());
    TfIdf_Data tfIdfClass;
    private PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;
    private String path;

    public VectorSpaceModel(String path){
        this.path = path;
        tfIdfClass = TfIdf_Data.getDefaultMap(path);
        corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
    }


    public ArrayList<ResultFormatter> getRankedClasses(Model model, ArrayList<String> queryString) {

        ArrayList<ResultFormatter> resultList = new ArrayList<ResultFormatter>();

        QueryVector queryVector = new QueryVector();
        HashMap<String, Double> queryMap = queryVector.getQueryVector(queryString);
        double query_norm = queryVector.getQueryNorm(queryMap);

        List<String> graphList = new ArrayList<String>();
        HashMap<String, Double> VectorSpaceScores = new HashMap<String, Double>();

        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

        /********************  Get All graphs in the Result Set *****************************/

        NodeIterator graphIterator = model.listObjectsOfProperty(graphProperty);

        while(graphIterator.hasNext()){
            String uri = graphIterator.next().toString();
            graphList.add(uri);
        }


        /*********************** Calculate VSM Value for each graph ***********************************/

        for (int i=0; i<graphList.size() ; i++){
            double doc_norm = 0.0;
            String graphIRI = graphList.get(i);

            logger.info(" /********** "+graphIRI +" *********/");

            DocNormCalculator docNormCal = new DocNormCalculator(path);
            doc_norm = docNormCal.getDocNormValue(graphIRI.toString());

            //logger.info(doc_norm+"");

            double TF_Value = 0.0;
            double IDF_Value =0.0;
            double TFIDF_Value =0.0;

            double score = 0.0;
            double finalScore = 0;

            for(Map.Entry<String, Double> entry: queryMap.entrySet()){

                String term = entry.getKey();
                double queryTermTFIDF = entry.getValue();
                double cumTF_IDF = 0.0;

                ArrayList<String> uriList = getURIsOfTerm(term,model);

                for(int j=0; j<uriList.size() ; j++){

                    String uri = uriList.get(j);

                    TF_IDFHolder tf_IdfHolder = getTF_IDFValues(uri, graphIRI);
                    TF_Value = tf_IdfHolder.getTF();
                    IDF_Value = tf_IdfHolder.getIDF();
                    TFIDF_Value = IDF_Value * TF_Value;

                    cumTF_IDF = cumTF_IDF + TFIDF_Value;
                }

                double DOC_TFIDF_FOR_TERM =  cumTF_IDF * queryTermTFIDF;
                score = score + DOC_TFIDF_FOR_TERM;
            }

            if (doc_norm != 0) {
                finalScore = score /(doc_norm * query_norm );
            }

            VectorSpaceScores.put(graphIRI, finalScore);
        }

        /*************************** Sort Hash map for vsm score **********************************/

        HashMap<String, Double> sortedBM25Map = sortByValues(VectorSpaceScores);

        /************************** put sorted values into ArrayList *******************************/

        for (Map.Entry entry : sortedBM25Map.entrySet()) {

            ResIterator uriIterator = model.listSubjectsWithProperty(graphProperty, model.createResource(entry.getKey().toString()));

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

    public ArrayList<String> getURIsOfTerm(String term, Model model){
        ArrayList<String> results = new ArrayList<String>();

        String sparql="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT ?uri "
                + " WHERE { "
                + " ?uri rdfs:label ?label."
                + " FILTER regex(?label, \""+ term +"\", \"i\")}";

        Query query = QueryFactory.create(sparql);

        QueryExecution exec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet resultset = exec.execSelect();
            while (resultset.hasNext()){
                QuerySolution qs = resultset.nextSolution();
                results.add(qs.get("uri").toString());
            }
        }catch(Exception e){
            logger.info(e+"");
        } finally {
            exec.close();
        }
        return results;
    }

    private TF_IDFHolder getTF_IDFValues(String term, String graphIRI){
        TF_IDFHolder tf_IdfHolder = new TF_IDFHolder();

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

        tf_IdfHolder = new TF_IDFHolder(tf, idf, tf_idf);
        return tf_IdfHolder;
    }

    private String getLabel(NodeIterator labelIterator, String propertyURI){

        String propLabel="";

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

    public double getDocNormValue(String docId){
        double doc_norm = 0.0;
        PrimaryTreeMap<String, Double> doc_norm_map;
        RecordManager recMan;

        String fileName = "doc_norm";
        try {
            recMan = RecordManagerFactory.createRecordManager(fileName);
            String recordName = "doc_norm_map";
            doc_norm_map = recMan.treeMap(recordName);
            doc_norm= doc_norm_map.get(docId);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.info("Can not get map because :" + e);
            //e.printStackTrace();
        }

        return doc_norm;
    }

}
