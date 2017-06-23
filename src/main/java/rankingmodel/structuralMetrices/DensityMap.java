package rankingmodel.structuralMetrices;

import java.io.IOException;
import java.util.HashMap;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class DensityMap {

    private static DensityMap defaultMap;

    private PrimaryTreeMap<String, HashMap<String, HashMap<String, Integer>>> density_map;

    private RecordManager recMan;

    public static DensityMap getDefaultMap(String path) {
        if(defaultMap==null) {
            defaultMap = new DensityMap(path);
        }
        return defaultMap;
    }

    public DensityMap(String path){

        String fileName = path + "density_map_database";
        try {
            recMan = RecordManagerFactory.createRecordManager(fileName);
            String recordName = "density_map_table";
            density_map = recMan.treeMap(recordName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void save_density_map(String ontologyId , HashMap<String, HashMap<String, Integer>> classStatistics) {
        // TODO Auto-generated method stub
        try {

            density_map.put(ontologyId, classStatistics);
            recMan.commit();

            /** close record manager */
            // recMan.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public PrimaryTreeMap<String, HashMap<String, HashMap<String, Integer>>> get_density_map() {
        return this.density_map;
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
