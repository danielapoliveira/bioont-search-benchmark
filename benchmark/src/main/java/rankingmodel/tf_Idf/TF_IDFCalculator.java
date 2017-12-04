package rankingmodel.tf_Idf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class TF_IDFCalculator {
    private Logger logger = Logger.getLogger(getClass().getName());

    TfIdf_Data tfIdfClass;

    String path;
    public TF_IDFCalculator(String path){
        this.path = path;
        tfIdfClass = TfIdf_Data.getDefaultMap(path);
    }
    public void tf_IdfCalculations() {

        TfIdf_Data map = TfIdf_Data.getDefaultMap(path);

        TF_IDFQueryAnalyzer query_analyzer= new TF_IDFQueryAnalyzer();


        try {
            /*********************************************************************
             * Get all ontologies of virtuoso repository in a list
             * *******************************************************************/
            ArrayList<String> ontologies= new ArrayList<String>();
            ontologies = query_analyzer.getExistingLoadedOntology();

		/* Size of ontology list is CORPUS_SIZE for tf_idf calculations. */
            double CORPUS_SIZE = 0;
            CORPUS_SIZE = ontologies.size();

		/* Considering each ontology one by on in a for loop, compute tf, Idf and tf_Idf values for all terms(IRIs) of that ontology*/

            for(int i=0; i<CORPUS_SIZE; i++) {

			/* Get ontology uri as ONTOLOGY_ID*/
                String ONTOLOGY_ID = "";
                ONTOLOGY_ID = ontologies.get(i);
                if (ONTOLOGY_ID.endsWith(".owl")) {
                    logger.info("******************* " + ONTOLOGY_ID + "**********************");
			/* For this ontology get all unique terms and number of times that term is in corpus as term-count in a hashmap of type <String, Integer>
			 * Here terms refers to IRIs only (i.e. excluding literals and blank nodes)*/
                    HashMap<String, Integer> termCountMap = new HashMap<String, Integer>();
                    termCountMap = query_analyzer.getTermCountForOntology(ONTOLOGY_ID);

			/* From termCountMap get maximum term count for a term that belongs to this ontology*/

                    double HIGHEST_FREQUENCY_TERM_COUNT = 0;
                    for (Map.Entry<String, Integer> entry : termCountMap.entrySet()) {
                        double value = Double.parseDouble(entry.getValue().toString());
                        if (value > HIGHEST_FREQUENCY_TERM_COUNT) {
                            HIGHEST_FREQUENCY_TERM_COUNT = value;
                        }
                        else {

                        }
                    }

                    //logger.info("HIGHEST_FREQUENCY_TERM_COUNT" + HIGHEST_FREQUENCY_TERM_COUNT);
			/* Initialize a hashmap "term_tf_IdfMapPerOntology".
			 * This map will store all the terms as key and their tf, Idf and tf_Idf values as a value. */

                    HashMap<String, HashMap<Integer, Double>> term_tf_IdfMapPerOntology = new HashMap<String, HashMap<Integer, Double>>();
	
			/* iterate termCountMap to get each term, find its values*/
                    for (Map.Entry<String, Integer> entry : termCountMap.entrySet()) {

                        double TERM_COUNT_PER_ONTOLOGY = 0;
				/*Initialize a hashmap that stores tf, Idf, tf_Idf values. 
				 * key is 1 for tf value, 2 for idf value and 3 for tf_Idf value */
                        HashMap<Integer, Double> tfIdfs = new HashMap<Integer, Double>();

				/*Term under consideration*/
                        String TERM = "";
                        TERM = entry.getKey().toString();
				
				/* Number of times that term appears in this ontology*/
                        TERM_COUNT_PER_ONTOLOGY = Double.parseDouble(entry.getValue().toString());

				/*Calculate tf value, TF (Term frequncy ) of any term is
				 * (Number of times that term appears in this ontology/ Maximum number of time any term appears in that ontology)*/
                        double TERM_FREQUENCY = 0.0;
                        TERM_FREQUENCY = 0.5 + ((0.5 * TERM_COUNT_PER_ONTOLOGY) / HIGHEST_FREQUENCY_TERM_COUNT);
				
				/* Add tf value to map*/
                        tfIdfs.put(1, TERM_FREQUENCY);

                        int TOTAL_NUMBER_OF_DOC_WITH_TERM = query_analyzer.getTermNumberOfDocContainingTerm(TERM);

				/*Calculate idf value, IDF (Inverse Document frequncy) of any term is
				 * log of (total number of ontologies in corpus / number of documents that contains this term in corpus )*/
                        double INVERSE_DOCUMENT_FREQUENCY = 0.0;
                        INVERSE_DOCUMENT_FREQUENCY = Math.log(CORPUS_SIZE / (1 + TOTAL_NUMBER_OF_DOC_WITH_TERM));
				
				/* Add idf value to map*/
                        tfIdfs.put(2, INVERSE_DOCUMENT_FREQUENCY);
				
				/* Calculate TFIDF value for this term
				 * TFIDF value of any term is = "Term frequency" / "Inverse document frequency" */
                        double TF_IDF = 0.0;
                        TF_IDF = TERM_FREQUENCY * INVERSE_DOCUMENT_FREQUENCY;
				
				/* Add tfidf value to map*/
                        tfIdfs.put(3, TF_IDF);
	
				/* Add this term and map containing tf, Idf, tf_Idf*/
                        term_tf_IdfMapPerOntology.put(TERM, tfIdfs);
                    }
			
			/*Once calculations are done for all terms of this ontology, 
			 * put this ontology and map contining tf_Idfs for all term into the map that is stored on the disk */
                    map.save_tfIdf_Value(ONTOLOGY_ID, term_tf_IdfMapPerOntology);
                    termCountMap.clear();
                    logger.info(ONTOLOGY_ID + " added into map " + i);
                }
            }
        } catch(Exception e){
            logger.info(e+"");
        } finally {
			/*Close connection 
			 * Very important to get connected next time*/
            //map.closeConnection();
        }
    }

}
