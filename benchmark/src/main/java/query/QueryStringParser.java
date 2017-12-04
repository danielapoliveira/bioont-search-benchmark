package query;

import test.Configuration;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Anila Sahar Butt.
 */
public class QueryStringParser {

    public ArrayList<String> parserQueryString(String queryString){

        ArrayList<String> queryWords = new ArrayList<>();
        ArrayList<String> stopWords = new ArrayList<>();

        try{

            FileInputStream fstream = new FileInputStream(Configuration.getProperty(Configuration.SAVE_PATH)+"stopList.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                stopWords.add(strLine);
            }
            in.close();

            // Print the content on the console
            StringTokenizer st = new StringTokenizer(queryString);

            while (st.hasMoreElements()) {
                String word = st.nextToken();
                if (stopWords.contains(word)){

                } else {
                    queryWords.add(word);
                }
            }


        }catch (Exception e){//Catch exception if any
            System.err.println("WAJA Error: " + e.getMessage());
            e.printStackTrace();

        }

        return queryWords;
    }

}
