package rankingmodel.tf_Idf;

import java.io.IOException;
import java.util.HashMap;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class TfIdf_Data {

	private static TfIdf_Data defaultMap;
	public PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;
	public RecordManager recMan;

	
	public static TfIdf_Data getDefaultMap(String path) {
		if(defaultMap==null) {
			defaultMap = new TfIdf_Data(path);
		}
		return defaultMap;
	}
	
	private TfIdf_Data(String path){
		String fileName = path + "corpus_tf_Idf_database";

		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "Corpus_termCount_ValueMap";
			corpus_tfIdf_Map = recMan.treeMap(recordName);
		} catch (IOException e) {
		}
	}
	
	public void save_tfIdf_Value(String ontologyId , HashMap<String, HashMap<Integer,Double>> ontologyTfIdfs) {
		// TODO Auto-generated method stub
		try {
			
			corpus_tfIdf_Map.put(ontologyId, ontologyTfIdfs);
		    recMan.commit();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> get_tfIdf_Value() {
		// TODO Auto-generated method stub
		return this.corpus_tfIdf_Map;
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
