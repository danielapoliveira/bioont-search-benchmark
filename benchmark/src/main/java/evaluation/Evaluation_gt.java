package evaluation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniela Oliveira on 22/03/2017.
 */
public class Evaluation_gt {

    String path;
    List<String> groundTruth;

    public Evaluation_gt(String path) throws IOException {
        this.path = path;
    }

    /**
     * Gets the results of P@k, AP@k and NDCG.
     * @param resultsFile map with the search results
     * @param k k to consider in the P@k and AP@k.
     * @throws IOException
     */
    public void calculate(HashMap<String,HashMap<String,List<List<String>>>> resultsFile, int k) throws IOException {
        HashMap<String,HashMap<String,HashMap<String,Double>>> evaluationMap =  new HashMap<>();
        HashMap<String,HashMap<String,HashMap<String,Double>>> ngtMap = new HashMap<>();

        for(Map.Entry<String,HashMap<String,List<List<String>>>> entry : resultsFile.entrySet() ){

            String term = entry.getKey();

            if(!evaluationMap.containsKey(term)) {
                evaluationMap.put(term, new HashMap<>());
            }

            HashMap<String,List<List<String>>> map = entry.getValue();

            NoGroundTruth ngt = new NoGroundTruth(path);
            ngtMap.put(term, ngt.calculate(map, k));
        }

        //Write evaluation to file
        FileWriter writer = new FileWriter(path+"evaluation2K"+ k +".tsv");
        writer.write("\t\tno gt precision\tno gt recall\n");
        for(Map.Entry<String,HashMap<String,HashMap<String,Double>>> entry : evaluationMap.entrySet()){
            writer.write(entry.getKey().trim()+"\t");
            for(Map.Entry<String,HashMap<String,Double>> entry2 : entry.getValue().entrySet()){
                writer.write(entry2.getKey()+"\t");
                writer.write(ngtMap.get(entry.getKey()).get("precision").get(entry2.getKey())+"\t");
                writer.write(ngtMap.get(entry.getKey()).get("recall").get(entry2.getKey())+"\t");
                writer.write("\n\t");
            }
           writer.write("\n");
        }
        writer.close();

    }

    /**
     * Adds the URIs contained in each query's ground truth file to a list.
     * @param groundTruthFile File with the ground truth results for one query term.
     * @return List of URIs in a ground truth file.
     * @throws IOException
     */
    private List<String> parseGroundTruthFile(File groundTruthFile) throws IOException {
        BufferedReader groundRead = new BufferedReader(new InputStreamReader(new FileInputStream(groundTruthFile)));
        List<String> groundList = new ArrayList<>();
        String line;
        while ((line = groundRead.readLine()) != null) {
            String uri = line.split("-")[0];
            groundList.add(uri.trim());
        }
        groundRead.close();
        return groundList;
    }

    /**
     * Calculates Precision@k.
     * @param relevant number of relevant hits.
     * @param k k to consider
     * @return value of the P@k
     */
    private double pak(float relevant, int k){
        return relevant / k;
    }



    private List<String> parseResults(List<List<String>> results){
        List<String> finalResults = new ArrayList<>();
        for(List<String> result : results){
            finalResults.add(result.get(0));
        }
        return finalResults;
    }



}
