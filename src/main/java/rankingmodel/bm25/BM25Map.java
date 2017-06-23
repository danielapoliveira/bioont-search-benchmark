package rankingmodel.bm25;

import java.io.IOException;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class BM25Map {

    private static BM25Map defaultMap;

    public static PrimaryTreeMap<String, Integer> bm25_ontology_lengths;
    public RecordManager recMan;


    public static BM25Map getDefaultMap(String path) {
        if(defaultMap==null) {
            defaultMap = new BM25Map(path);
        }
        return defaultMap;
    }

    private BM25Map(String path){

        String fileName = path+"bm25_ontology_lengths";
        try {
            recMan = RecordManagerFactory.createRecordManager(fileName);
            String recordName = "bm25_ontology_lengths_maps";
            bm25_ontology_lengths = recMan.treeMap(recordName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save_bm25_ontology_lengths_Value(String ontologyId , int count) {
        try {
            bm25_ontology_lengths.put(ontologyId, count);
            recMan.commit();

            /** close record manager */
            //recMan.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public PrimaryTreeMap<String, Integer> get_bm25_ontology_lengths_Value() {
        return bm25_ontology_lengths;
    }

    public int getDocLength(String ontologyIRI){
        int length=0;
        try {
            length=bm25_ontology_lengths.get(ontologyIRI);
        } catch(Exception e) {
            System.out.println(e);
        }

        return length;
    }

    public void closeConnection(){
        try {
            recMan.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
