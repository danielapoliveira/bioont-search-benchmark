package rankingmodel.vectorspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import rankingmodel.tf_Idf.TF_IDFQueryAnalyzer;
import rankingmodel.tf_Idf.TfIdf_Data;
import jdbm.PrimaryTreeMap;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class DocNormCalculator {

    private Logger logger = Logger.getLogger(getClass().getName());
    TfIdf_Data tfIdfClass;
    PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;

    String path;

    public DocNormCalculator(String path){
        this.path = path;
        tfIdfClass = TfIdf_Data.getDefaultMap(path);
        corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
    }

    public void saveDocNormforCorpus(){
        TF_IDFQueryAnalyzer query_analyzer= new TF_IDFQueryAnalyzer();
        DocumentNormMap diskmap = new DocumentNormMap(path);
        try {

            ArrayList<String> ontologies = query_analyzer.getExistingLoadedOntology();
            for(int i=0;i<ontologies.size();i++){
                String graphIRI = ontologies.get(i);

                double count =  calculateDocumentNorm(graphIRI);
                //logger.info("For Ontology "+i+" : "+ graphIRI+"  doc_norm is : " + count);
                diskmap.save_doc_norm_map(graphIRI, count);
            }
        } catch(Exception e) {
            logger.info("can not save doc norm because :" + e);
        }finally {
            //diskmap.closeConnection();
        }
    }


    public double calculateDocumentNorm(String graphIRI){

        HashMap<String, HashMap<Integer,Double>> tf_Idfs = corpus_tfIdf_Map.get(graphIRI);
        double doc_norm = 0.0 ;
        String term ="";
        try{
            for (Map.Entry<String, HashMap<Integer,Double>> entry:tf_Idfs.entrySet()){
                term = entry.getKey();
                HashMap<Integer,Double> tf_Idf_map = entry.getValue();
                double score = tf_Idf_map.get(3);
                doc_norm = doc_norm + Math.pow(score,2);
            }
            doc_norm = Math.sqrt(doc_norm);
            //logger.info(doc_norm+"");
        } catch(Exception e) {
            logger.info("can not calculate  doc norm because :" + e +term);
        }finally {

        }
        return doc_norm;
    }

    public double getDocNormValue(String docId){
        //System.out.println("docId: "+docId);
        double doc_norm = 0.0;
        DocumentNormMap docNormMap = new DocumentNormMap(path);
        try {
            PrimaryTreeMap<String,Double> map = docNormMap.get_doc_norm_map();
            if (map.containsKey(docId)) {
                doc_norm = map.get(docId);
            } else { }
        } catch (Exception e) {
            System.out.println( "can not find doc norm value becasue : " + e);
        } finally {
            //docNormMap.closeConnection();
        }
        return doc_norm;
    }

}
