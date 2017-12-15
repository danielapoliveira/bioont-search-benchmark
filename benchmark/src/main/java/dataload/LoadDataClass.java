package dataload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anila Sahar Butt.
 */
public class LoadDataClass {

    private String path;

    public LoadDataClass(String path){
        this.path = path;
    }

    /*public void loadData(ArrayList<String> graphs) throws Exception {

        OntologyPersistance ontoPersistance = new OntologyPersistance();

        //System.out.println("Testing 1 - Send Http GET request");

        File file = new File("userinput/Ontologies_not_loaded.txt");
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        List<String> identifiers = new ArrayList<String>();
        for (int i=0; i<graphs.size(); i++){
            String graphIRI = graphs.get(i);
            System.out.println("***************** Getting Values for Graph No"+ i +" : Graph URI is :"+graphIRI+"******************");

            boolean insertCheck = ontoPersistance.loadOntologyIntoVirtuosoRespository(graphIRI, identifiers);
            ontoPersistance.insertMetadata(graphIRI, identifiers);
            if(insertCheck == false){
                bw.write(graphIRI+"\n");
            }
        }
        bw.close();
    }*/

    public void loadMetaData(ArrayList<String> graphs) throws Exception {

        OntologyPersistance ontoPersistance = new OntologyPersistance();
        List<String> identifiers = new ArrayList<String>();
        for (int i=0; i<graphs.size(); i++){
            String graphIRI = graphs.get(i);
                System.out.println("***************** Getting Values for Graph No" + i + " : Graph URI is :" + graphIRI + "******************");

                ontoPersistance.insertMetadata(graphIRI, identifiers);

        }

    }
}