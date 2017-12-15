package evaluation;

import test.Configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class NoGroundTruth_v0 {

    String savePath;
    HashMap<String,HashMap<String,Integer>> evaluationMap = new HashMap<>();

    public NoGroundTruth_v0(String savePath){
        this.savePath = savePath;
    }

    public HashMap<String,HashMap<String,Double>> calculate(HashMap<String,List<List<String>>> resultsFile, int k) throws IOException {
        createProbabilityMap(resultsFile.keySet());
        for(Map.Entry<String,List<List<String>>> entry : resultsFile.entrySet() ) {
            String algorithm = entry.getKey();
            List<List<String>> searchResults = entry.getValue();
            List<String> parseResults = parseResults(searchResults);
            int i = 0;
            while(i < k && i < parseResults.size()){
                String result = parseResults.get(i);
                String ontology = getOntologyFromUri(result);
                HashMap<String,Integer> status = evaluationMap.get(ontology);
                status.put(algorithm, 1);
                evaluationMap.put(ontology, status);
                i++;
            }
        }


        float sumAllProb = 0;
        HashMap<String,Double> probabilityMap = new HashMap<>();
        HashMap<String,Integer> systemSum = new HashMap<>();
        for(Map.Entry<String, HashMap<String,Integer>> entry : evaluationMap.entrySet()){
            String ontology = entry.getKey();
            HashMap<String,Integer> hits = entry.getValue();
            int sumHits = getProbabilityOntology(hits);
            double hitsSize = hits.size();
            double probability = sumHits / hitsSize;
            probabilityMap.put(ontology,probability);
            sumAllProb += probability;

            for(Map.Entry<String,Integer> entry2 : hits.entrySet()){
                if(!systemSum.containsKey(entry2.getKey())) {
                    systemSum.put(entry2.getKey(), entry2.getValue());
                }
                else{
                    systemSum.put(entry2.getKey(), systemSum.get(entry2.getKey())+entry2.getValue());
                }
            }
        }
        int numOntologies = evaluationMap.keySet().size();

        // sum P Sk
        HashMap<String,Double> systemsProb = new HashMap<>();
        for(Map.Entry<String,Integer> entry : systemSum.entrySet()){
            double probability = (double) entry.getValue() / numOntologies;
            systemsProb.put(entry.getKey(),probability);
        }

        HashMap<String,Double> precisionMap = new HashMap<>();
        HashMap<String,Double> recallMap = new HashMap<>();
        for(Map.Entry<String,Double> entry : systemsProb.entrySet()){
            double precision =  entry.getValue() / systemSum.get(entry.getKey());
            //System.out.println(precision);
            precisionMap.put(entry.getKey(),precision);

            double recall = entry.getValue() / sumAllProb;
            recallMap.put(entry.getKey(), recall);
        }

        HashMap<String,HashMap<String,Double>> finalResult = new HashMap<>();
        finalResult.put("precision", precisionMap);
        finalResult.put("recall", recallMap);
        return finalResult;
    }

    private HashMap<String,Integer> getSystemSum(HashMap<String,Integer> ontologyHits){
        HashMap<String,Integer> systemSum = new HashMap<>();
        for(Map.Entry<String,Integer> entry : ontologyHits.entrySet()){
            String system = entry.getKey();
            if(!systemSum.containsKey(system)){
                systemSum.put(system,1);
            }
            else{
                systemSum.put(system,systemSum.get(system)+1);
            }
        }
        return systemSum;
    }

    private int getProbabilityOntology(HashMap<String,Integer> ontologyHits){
        int hits = 0;
        for(Map.Entry<String,Integer> entry : ontologyHits.entrySet()){
            hits += entry.getValue();
        }
        return hits;
    }

    private String getOntologyFromUri(String uri){
        String[] tmpSplit = uri.split("/");
        String ontology = tmpSplit[(tmpSplit.length-1)].split("_")[0];
        return ontology;
    }

    private void createProbabilityMap(Set<String> algorithms) throws IOException {
        List<String> acronyms = getAcronyms();
        for(String acronym : acronyms) {
            HashMap<String,Integer> algorithmMap = new HashMap<>();
            for(String algorithm : algorithms){
                algorithmMap.put(algorithm, 0);
            }
            algorithmMap.put("all", 1);
            algorithmMap.put("none", 0);
            evaluationMap.put(acronym, algorithmMap);
        }
    }

    private List<String> getAcronyms() throws IOException {
        List<String> acronyms = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Configuration.getProperty(Configuration.ACRONYM_PATH))));
        String line;
        while ((line = reader.readLine()) != null) {
            acronyms.add(line.trim());
        }

        reader.close();
        return acronyms;
    }

    public List<String> parseResults(List<List<String>> results){
        List<String> finalResults = new ArrayList<>();
        for(List<String> result : results){
            finalResults.add(result.get(0));
        }
        return finalResults;
    }
}
