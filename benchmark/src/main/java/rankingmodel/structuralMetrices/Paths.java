package rankingmodel.structuralMetrices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class Paths {
	
	private static Paths defaultMap;
	
	private PrimaryTreeMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> paths;
	
	private RecordManager recMan;

	public static Paths getDefaultMap(String path) {
		if(defaultMap==null) {
			defaultMap = new Paths(path);
		}
		return defaultMap;
	}
	
	public Paths(String path){
		String fileName = path + "paths_database";

		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "path_map_table";
			paths = recMan.treeMap(recordName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save_adjacency_matrix_map(String sourceClass , HashMap<String, HashMap<String, ArrayList<String>>> outlinks) {
		// TODO Auto-generated method stub
		try {

			paths.put(sourceClass, outlinks);
			recMan.commit();

			/** close record manager */
			// recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void commit(){
		try {
			recMan.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public PrimaryTreeMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> get_adjacency_matrix_map() {
		return this.paths;
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
