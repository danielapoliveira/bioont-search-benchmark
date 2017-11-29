package rankingmodel.bm25;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import jdbm.PrimaryTreeMap;

/**
 * Created by Anila Sahar Butt.
 */
public class BM25Calculator {

    private ArrayList<String> ontologies= new ArrayList<String>();
    BM25_QueryAnalyzer query_analyzer= new BM25_QueryAnalyzer();
    BM25Map diskmap;
    private Logger logger = Logger.getLogger(getClass().getName());

    String path;

    public BM25Calculator(String path){
        this.path = path;
        diskmap = BM25Map.getDefaultMap(path);
    }

    public void calculateOntologyTermStatistics(){

        try {
            ontologies = query_analyzer.getExistingLoadedOntology();

            for(int i=0;i<ontologies.size();i++){

                String graphIRI = ontologies.get(i);
                if(graphIRI.endsWith(".owl")) {
                    int count = query_analyzer.getTotalOntologyTerms(graphIRI);

                    logger.info("********** For Ontology " + i + " : " + graphIRI + "  number of terms : " + count);

                    diskmap.save_bm25_ontology_lengths_Value(graphIRI, count);
                }
            }
        } catch(Exception e){
            logger.info(e.toString());
        } finally{
            //diskmap.closeConnection();
        }
    }


    public long calculateAverageDocumentLength(){

        PrimaryTreeMap<String, Integer> map = diskmap.get_bm25_ontology_lengths_Value();
        long avgLength = 0;
        long totalLength= 0;

        int numberOFontologies =0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            totalLength= totalLength + Integer.parseInt(entry.getValue().toString());
            numberOFontologies++;
        }

        avgLength = (totalLength/numberOFontologies);
        logger.info("Total Lenght" + totalLength);
        logger.info("Total Ontologies" + numberOFontologies);
        logger.info("Average Lenght" + avgLength);

        return avgLength;
    }
}
