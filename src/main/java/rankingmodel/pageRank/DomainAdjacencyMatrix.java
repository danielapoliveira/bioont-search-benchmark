package rankingmodel.pageRank;

import java.io.IOException;
import java.util.ArrayList;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt.
 */
public class DomainAdjacencyMatrix {

	private static DomainAdjacencyMatrix defaultMap;

	private PrimaryTreeMap<String,ArrayList<String>> domain_adjacency_matrix_map;

	private RecordManager recMan;

	public static DomainAdjacencyMatrix getDefaultMap(String path) {
		if(defaultMap==null) {
			defaultMap = new DomainAdjacencyMatrix(path);
		}
		return defaultMap;
	}

	public DomainAdjacencyMatrix(String path){

		String fileName = path + "domain_adjacency_matrix_map_database";
		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "domain_adjacency_matrix_map_table";
			domain_adjacency_matrix_map = recMan.treeMap(recordName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save_domain_adjacency_matrix_map(String sourceClass , ArrayList<String> outlinks) {
		// TODO Auto-generated method stub
		try {

			domain_adjacency_matrix_map.put(sourceClass, outlinks);
			recMan.commit();

			/** close record manager */
			// recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public PrimaryTreeMap<String,ArrayList<String>> get_domain_adjacency_matrix_map() {
		return this.domain_adjacency_matrix_map;
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
