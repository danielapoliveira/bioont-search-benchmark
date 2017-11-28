package evaluation;

import utils.MapUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoGroundTruth_v3 {

    String savePath;
    //system=<algorithm,boolean>
    HashMap<String,HashMap<String,Boolean>> evaluationMap = new HashMap<>();

    public NoGroundTruth_v3(String savePath){
        this.savePath = savePath;
    }

    public HashMap<String,HashMap<String,Double>> calculate(HashMap<String,List<List<String>>> resultsFile, int k) throws IOException {

        createProbabilityMap(resultsFile.keySet());
        // Update the map for each system to change the document value if the query exists in that document.
        for(Map.Entry<String,List<List<String>>> entry : resultsFile.entrySet() ) {
            String algorithm = entry.getKey();
            List<List<String>> searchResults = entry.getValue();
            List<String> parseResults = parseResults(searchResults);
            int i = 0;
            while(i < k && i < parseResults.size()){
                String result = parseResults.get(i);
                String ontology = getOntologyFromUri(result);
                HashMap<String,Boolean> status = evaluationMap.get(algorithm);
                status.put(ontology, true);
                evaluationMap.put(algorithm, status);
                i++;
            }
        }

        HashMap<String,Double> ontologyProbabilityMap = getOntologyProbabilityMap(resultsFile.keySet().size());
        //System.out.println(ontologyProbabilityMap);

        String gt_path = "C:\\Users\\danoli\\Desktop\\CBRBench\\bioont-search-benchmark\\userinput\\ground_truth\\";
        List<String> gt_files = getListOfFiles(gt_path, ".tsv");

        for(String file : gt_files){
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("C:\\Users\\danoli\\Desktop\\CBRBench\\bioont-search-benchmark\\userinput\\no_gt\\"+file), "utf-8"))) {

                List<String> readFile = readFiletoList(gt_path + file);
                Map<String,Double> ranking = new HashMap<>();
                for (String line : readFile) {
                    String[] split_line = line.split("\t");
                    String uri = split_line[0].split(" - ")[0];
                    String acro = getOntologyFromUri(split_line[0].split(" - ")[0]);
                    Double probability = ontologyProbabilityMap.get(acro);
                    ranking.put(split_line[0], probability);
                }
                Map<String,Double> sortedRankings = MapUtil.sortByValue(ranking);
                for(Map.Entry<String,Double> entry : sortedRankings.entrySet()){
                    writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
                }
            }
        }

        HashMap<String,HashMap<String,Double>> finalResult = new HashMap<>();
        return finalResult;

    }

    private List<String> readFiletoList(String fileName){
        List<String> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            //1. filter line 3
            //2. convert all content to upper case
            //3. convert it into a List
            list = stream
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\danoli\\Google Drive\\CBRBench\\bioont-search-benchmark\\userinput\\acronyms.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            acronyms.add(line.trim());
        }

        reader.close();
        return acronyms;
    }

    private List<String> getListOfFiles(String path, String endswith) throws IOException {
        List<String> results = new ArrayList<>();
        File[] files = new File(path).listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(endswith)) {
                results.add(file.getName());
            }
        }
        return results;
    }

    public List<String> parseResults(List<List<String>> results){
        List<String> finalResults = new ArrayList<>();
        for(List<String> result : results){
            finalResults.add(result.get(0));
        }
        return finalResults;
    }
}
