package evaluation;

import java.io.*;
import java.util.*;

/**
 * Created by Daniela Oliveira on 22/03/2017.
 */
public class Evaluation {

    String path;
    List<String> groundTruth;

    public Evaluation(String path) throws IOException {
        this.path = path;
    }

    /**
     * Gets the results of P@k, AP@k and NDCG.
     * @param resultsFile map with the search results
     * @param groundPath path for the ground truth file
     * @param k k to consider in the P@k and AP@k.
     * @throws IOException
     */
    public void calculate(HashMap<String,HashMap<String,List<List<String>>>> resultsFile, String groundPath, int k) throws IOException {
        HashMap<String, Double> mapk = new HashMap<>();
        HashMap<String,HashMap<String,HashMap<String,Double>>> evaluationMap =  new HashMap<>();

        for(Map.Entry<String,HashMap<String,List<List<String>>>> entry : resultsFile.entrySet() ){
            String term = entry.getKey();

            if(!evaluationMap.containsKey(term)) {
                evaluationMap.put(term, new HashMap<>());
            }

            HashMap<String,List<List<String>>> map = entry.getValue();
            groundTruth = parseGroundTruthFile(new File(groundPath+term+".tsv"));
            for(Map.Entry<String,List<List<String>>> entry2 : map.entrySet()){
                float relevant = 0;
                String algorithm = entry2.getKey();

                if(!evaluationMap.get(term).containsKey(algorithm)) {
                    evaluationMap.get(term).put(algorithm,new HashMap<>());
                }

                List<List<String>> searchResults = entry2.getValue();
                List<String> parseResults = parseResults(searchResults);

                double ndcg = NDCG.compute(parseResults,groundTruth,null);
                evaluationMap.get(term).get(algorithm).put("ndcg",ndcg);

                double apk = MAP.apk(parseResults,groundTruth,k);
                evaluationMap.get(term).get(algorithm).put("apk",apk);
                if(!mapk.keySet().contains(algorithm)){
                    mapk.put(algorithm,apk);
                }
                else{
                    mapk.put(algorithm,mapk.get(algorithm)+apk);
                }

                int i = 0;
                while(i < k && i < searchResults.size()){
                    List<String> result = searchResults.get(i);
                    if(groundTruth.contains(result.get(0)))
                        relevant++;
                    i++;
                }
                evaluationMap.get(term).get(algorithm).put("pak",pak(relevant,k));
            }
        }

        HashMap<String,Double> mapkMap = new HashMap<>();
        for(Map.Entry<String,Double> entry : mapk.entrySet()){
            double apk = entry.getValue();
            double mapkFinal = apk / resultsFile.keySet().size();
            mapkMap.put(entry.getKey(),mapkFinal);
        }
        FileWriter writer = new FileWriter(path+"evaluationK"+ k +".tsv");
        writer.write("\t\tndcg\tpak\tapk\tmap\n");
        for(Map.Entry<String,HashMap<String,HashMap<String,Double>>> entry : evaluationMap.entrySet()){
            writer.write(entry.getKey().trim()+"\t");
            for(Map.Entry<String,HashMap<String,Double>> entry2 : entry.getValue().entrySet()){
                writer.write(entry2.getKey()+"\t");
                HashMap<String,Double> eval = entry2.getValue();
                writer.write(eval.get("ndcg")+"\t");
                writer.write(eval.get("pak")+"\t");
                writer.write(eval.get("apk")+"\t");

                writer.write(mapkMap.get(entry2.getKey()).toString());
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
