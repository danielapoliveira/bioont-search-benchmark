package rankingmodel.vectorspace;

import java.io.IOException;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class DocumentNormMap {

	private static DocumentNormMap defaultMap;

	private PrimaryTreeMap<String, Double> doc_norm_materialized_map;

	private RecordManager recMan;

	public static DocumentNormMap getDefaultMap(String rankingPath) {
		if(defaultMap==null) {
			defaultMap = new DocumentNormMap(rankingPath);
		}
		return defaultMap;
	}

	public DocumentNormMap(String rankingPath){

		String fileName = rankingPath+"document_norm_database";
		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "document_norm_table";
			doc_norm_materialized_map = recMan.treeMap(recordName);
			System.out.println("connection established :");
		} catch (IOException e) {
			System.out.println("can not connect because of :" + e);}
	}

	public void save_doc_norm_map(String ontologyId , double count) {
		try {

			doc_norm_materialized_map.put(ontologyId, count);
			recMan.commit();

			/** close record manager */
			 recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public PrimaryTreeMap<String, Double> get_doc_norm_map() {
		return this.doc_norm_materialized_map;
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
