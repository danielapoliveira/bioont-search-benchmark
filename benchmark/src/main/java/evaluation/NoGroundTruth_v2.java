package evaluation;

import test.Configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class NoGroundTruth_v2 {

    String savePath;
    //system=<algorithm,boolean>
    HashMap<String,HashMap<String,Boolean>> evaluationMap = new HashMap<>();

    public NoGroundTruth_v2(String savePath){
        this.savePath = savePath;
    }

    public HashMap<String,HashMap<String,Double>> calculate(HashMap<String,List<List<String>>> resultsFile, int k) throws IOException {

        // Update the map for each system to change the document value if the query exists in that document.
        for(Map.Entry<String,List<List<String>>> entry : resultsFile.entrySet() ) {
            String algorithm = entry.getKey();
                if (!evaluationMap.containsKey(algorithm)) {
                    evaluationMap.put(algorithm, new HashMap<>());

                List<List<String>> searchResults = entry.getValue();
                List<String> parseResults = parseResults(searchResults);

                int i = 0;
                while (i < k && i < parseResults.size()) {
                    String result = parseResults.get(i);
                    HashMap<String, Boolean> status = evaluationMap.get(algorithm);
                    status.putIfAbsent(result, true);
                    evaluationMap.put(algorithm, status);
                    i++;
                }
            }
        }
        HashMap<String,Double> ontologyProbabilityMap = getOntologyProbabilityMap(resultsFile.keySet().size());
        //System.out.println(ontologyProbabilityMap);
        double documentProbabilitySum = getOntologyProbabilitySum(ontologyProbabilityMap);

        HashMap<String,Double> systemProbabilityMap = new HashMap<>();
        HashMap<String,Double> systemSumMap = new HashMap<>();
        for(Map.Entry<String, HashMap<String,Boolean>> entry : evaluationMap.entrySet()){
            String system = entry.getKey();
            HashMap<String,Boolean> documentsValue = entry.getValue();
            double systemSum = getSystemSum(documentsValue);
            systemSumMap.put(system, systemSum);

            double ontologyProbabilitySum = getProbabilityOfRelevance(ontologyProbabilityMap,system);
            systemProbabilityMap.put(system, ontologyProbabilitySum);
        }

        HashMap<String,Double> precisionMap = new HashMap<>();
        HashMap<String,Double> recallMap = new HashMap<>();

        //System.out.println(systemSumMap);
        //System.out.println(systemProbabilityMap);
        //System.out.println(documentProbabilitySum);
        for(Map.Entry<String,Double> entry : systemProbabilityMap.entrySet()){
            double precision =  entry.getValue() / systemSumMap.get(entry.getKey());
            precisionMap.put(entry.getKey(),precision);

            double recall = entry.getValue() / documentProbabilitySum;
            recallMap.put(entry.getKey(), recall);
        }

        HashMap<String,HashMap<String,Double>> finalResult = new HashMap<>();
        finalResult.put("precision", precisionMap);
        //System.out.println(precisionMap);
        //System.out.println(recallMap);
        //System.out.println();
        finalResult.put("recall", recallMap);
        return finalResult;
    }

    private double getProbabilityOfRelevance(HashMap<String,Double> ontologyProbabilityMap, String system){
        double probabilitySum = 0;
        //System.out.println(system);

        for(Map.Entry<String,Boolean> entry : evaluationMap.get(system).entrySet()){
            if(entry.getValue()){
                //System.out.println(ontologyProbabilityMap);
                //System.out.println(entry.getKey());
                //System.out.println(entry.getValue());
                double ontologyProbability = ontologyProbabilityMap.get(entry.getKey());
                //System.out.println(ontologyProbability);
                probabilitySum += ontologyProbability;
            }
        }
        //System.out.println();
        return probabilitySum;
    }
    private HashMap<String,Double> getOntologyProbabilityMap(int numSystems){

        HashMap<String,Double> ontologyMap = new HashMap<>();
        for(Map.Entry<String, HashMap<String,Boolean>> entry : evaluationMap.entrySet()){
            for(Map.Entry<String,Boolean> entry2 : entry.getValue().entrySet()){
                String ontology = entry2.getKey();
                if(entry2.getValue()) {
                    if (!ontologyMap.containsKey(ontology)) {
                        ontologyMap.put(ontology, 1.0);
                    } else {
                        ontologyMap.put(ontology,ontologyMap.get(ontology)+1.0);
                    }
                }
            }

        }
        HashMap<String,Double> ontologyProbabilityMap = new HashMap<>();
        for(Map.Entry<String,Double> entry3 : ontologyMap.entrySet()){
            double probability = entry3.getValue() / (numSystems + 2);
            ontologyProbabilityMap.put(entry3.getKey(),probability);

        }
        //System.out.println(ontologyProbabilityMap);
        //System.out.println();
        return ontologyProbabilityMap;
    }

    private double getOntologyProbabilitySum(HashMap<String,Double> map){
        double probabilitySum = 0;
        for(Map.Entry<String,Double> entry : map.entrySet()){
            probabilitySum += entry.getValue();
        }
        return probabilitySum;
    }

    private double getSystemSum(HashMap<String,Boolean> documentsValue){
        double hits = 0.0;
        List<String> ontologiesCounted = new ArrayList<>();
        for(Map.Entry<String,Boolean> entry : documentsValue.entrySet()){
            if(entry.getValue() && !ontologiesCounted.contains(entry.getKey())) {
                hits += 1.0;
                ontologiesCounted.add(entry.getKey());
            }
        }
        return hits;
    }

    private String getOntologyFromUri(String uri){
        String[] tmpSplit = uri.split("/");
        String ontology = tmpSplit[(tmpSplit.length-1)].split("_")[0];
        return ontology;
    }

    private void createProbabilityMap(Set<String> algorithms) throws IOException {
        for(String algorithm : algorithms){
            evaluationMap.put(algorithm,buildAcronymMap(false));
        }

        evaluationMap.put("all",buildAcronymMap(true));

        evaluationMap.put("none",buildAcronymMap(false));
    }

    private HashMap<String,Boolean> buildAcronymMap(boolean state) throws IOException {
        List<String> acronyms = getAcronyms();
        HashMap<String,Boolean> acronymMap = new HashMap<>();
        for(String acronym : acronyms){
            acronymMap.put(acronym, state);
        }
        return acronymMap;
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
