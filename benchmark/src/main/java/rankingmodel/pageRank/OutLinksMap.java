package rankingmodel.pageRank;

import java.io.IOException;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt.
 */
public class OutLinksMap {
	
	private static OutLinksMap defaultMap;
	
	private PrimaryTreeMap<String,Integer> outlinks_map;
	
	private RecordManager recMan;

	public static OutLinksMap getDefaultMap(String path) {
		if(defaultMap==null) {
			defaultMap = new OutLinksMap(path);
		}
		return defaultMap;
	}
	
	public OutLinksMap(String path){
		
		String fileName = path + "outlinks_map_database";
		try {
			recMan = RecordManagerFactory.createRecordManager(fileName);
			String recordName = "outlinks_map_table";
			outlinks_map = recMan.treeMap(recordName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save_outlinks_map(String sourceClass , int outlinks) {
		// TODO Auto-generated method stub
		try {
			
			outlinks_map.put(sourceClass, outlinks);
		    recMan.commit();
		    
		    /** close record manager */
		   // recMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public PrimaryTreeMap<String,Integer> get_outlinks_map() {
		return this.outlinks_map;
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
