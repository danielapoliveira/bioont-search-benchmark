package dataload;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import store.QuadStore;

/**
 * Created by Anila Sahar Butt.
 */
public class OntologyPersistance {


	QuadStore store = QuadStore.getDefaultStore();
	MetaDataFactory metadata = new MetaDataFactory();
	ImportsFinder io = new ImportsFinder();
	private Logger logger;

	public OntologyPersistance(){
		logger = Logger.getLogger(getClass().getName());
	}

	public boolean loadOntologyIntoVirtuosoRespository(String urlString, List<String> identifiers){

		//load ontology to the store
		logger.info(urlString+ " loading ...");
		boolean insertCheck = store.insert(urlString);


		if(insertCheck == false)
		{
			insertCheck = store.loadGraph(urlString);
		}
		// check if ontology has loaded into the store
		if (insertCheck == true){

			//add metadata about this ontology (i.e. graph iri, identifier and date of load into store)
			metadata.addMetadata(urlString, identifiers);

			//get a list of ontologies that has been imported by ontology in hand.
			ArrayList<String> ontologiesList = io.getImportOntologies(urlString);

			//if there are some ontologies that are imported by this ontology
			if (ontologiesList.size() >0){

				//for each imported ontology
				for (int i = 0; i<ontologiesList.size() ; i++){
					logger.info( urlString + "		imports		" + ontologiesList.get(i));

					// load this ontology into virtuoso repository and add its metadata into store using this recursive function.
					List<String> importOntologiesId = new ArrayList<>();
					loadOntologyIntoVirtuosoRespository(ontologiesList.get(i), importOntologiesId);
				}
				metadata.addImportsMetadata(urlString, ontologiesList);
			}
		}
		return insertCheck;
	}

	public void insertMetadata(String urlString, List<String> identifiers){
        //add metadata about this ontology (i.e. graph iri, identifier and date of load into store)
        metadata.addMetadata(urlString, identifiers);

        //get a list of ontologies that has been imported by ontology in hand.
        ArrayList<String> ontologiesList = io.getImportOntologies(urlString);

        //if there are some ontologies that are imported by this ontology
        if (ontologiesList.size() >0){

            //for each imported ontology
            for (int i = 0; i<ontologiesList.size() ; i++){
                logger.info( urlString + "		imports		" + ontologiesList.get(i));

                // load this ontology into virtuoso repository and add its metadata into store using this recursive function.
                List<String> importOntologiesId = new ArrayList<>();
                loadOntologyIntoVirtuosoRespository(ontologiesList.get(i), importOntologiesId);
            }
            metadata.addImportsMetadata(urlString, ontologiesList);
        }
    }

}

