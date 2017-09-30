package rankingmodel.pageRank;

import java.io.IOException;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class PageRankMap {
	
	private static PageRankMap defaultMap;
	
	private PrimaryTreeMap<String, Double> pagerank_map;
	
	private RecordManager recMan;

	public static PageRankMap getDefaultMap(String path) {
		if(defaultMap==null) {
			defaultMap = new PageRankMap(path);
		}
		return defaultMap;
	}
	
	private PageRankMap(String path){

		String fileName = path + "pagerank_database";
		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "pagerank_table";
			pagerank_map = recMan.treeMap(recordName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save_tf_Idf_map(String graphIRI , Double tf_Idf_value) {
		// TODO Auto-generated method stub
		try {
			
			pagerank_map.put(graphIRI, tf_Idf_value);
		    recMan.commit();
		    
		    /** close record manager */
		    recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public PrimaryTreeMap<String, Double> get_tf_Idf_map() {
		return this.pagerank_map;
	}
	
	public void closeConnection(){
		try {
			recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
 	
 	public long getTotalNumberOfNodes(){
 		long total_num_nodes = 0;
 		try {
 			total_num_nodes = pagerank_map.size();		
 		} catch(Exception e) {
 			System.out.println(e);
 		}

		return total_num_nodes;
 	}
 	
 	public void commitValues() {
		// TODO Auto-generated method stub
		try {

		    recMan.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
