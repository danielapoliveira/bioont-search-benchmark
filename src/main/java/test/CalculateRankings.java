package test;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.*;
import searchapps.SearchApps;
import searchapps.SearchResult;

import jdbm.PrimaryTreeMap;

import query.MeasureDAO;
import query.QueryStringParser;
import query.ResultFormatter;
import rankingmodel.bm25.BM25Model;
import rankingmodel.pageRank.PageRank;
import rankingmodel.booleanModel.BooleanModel;
import rankingmodel.structuralMetrices.ClassMatchMeasure;
import rankingmodel.structuralMetrices.SemanticSimilarityMeasure;
import rankingmodel.tf_Idf.TF_IDFModel;
import rankingmodel.tf_Idf.TfIdf_Data;
import rankingmodel.vectorspace.VectorSpaceModel;

/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class CalculateRankings {
    private static TfIdf_Data tfIdfClass;
    private static PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;
    private String path;
    private Logger logger= Logger.getLogger(getClass().getName());

    public CalculateRankings(String path){
        this.path = path;
        tfIdfClass= TfIdf_Data.getDefaultMap(path);
        corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();
    }

    public HashMap<String,HashMap<String,List<List<String>>>> search(String queryFile,String resultPath) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(queryFile))));

        HashMap<String,HashMap<String,List<List<String>>>> finalMap = new HashMap<>();


        String query;
        HashMap<String, Double> classMatchScoreMap = new HashMap<String, Double>();
        while ((query = br.readLine()) != null) {
            QueryStringParser stringParser = new QueryStringParser();
            ArrayList<String> queryWords = stringParser.parserQueryString(query);

            finalMap.put(query, new HashMap<>());
            logger.info("Query words: "+query);
            String fileName = resultPath + query + ".tsv";
            MeasureDAO measureDao = new MeasureDAO();



            FileWriter writer = new FileWriter(fileName);

            Model model = measureDao.getSearchResults(queryWords);

            BooleanModel rankModel = new BooleanModel();
            ArrayList<ResultFormatter> rankedClassList = rankModel.getRankedClasses(model);
            HashMap<String, ArrayList<String>> map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append('\n');
            writer.append("boolean");
            writer.append('\n');
            finalMap.get(query).put("boolean", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("boolean").add(tmpList);
            }
            writer.append('\n');
            map.clear();

            System.out.println("Starting TF-IDF...");
            TF_IDFModel rankModel2 = new TF_IDFModel(path);
            rankedClassList = rankModel2.getRankedClasses(model);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("tf_idf");
            writer.append('\n');
            finalMap.get(query).put("tf_idf", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("tf_idf").add(tmpList);
            }
            writer.append('\n');
            map.clear();

            System.out.println("Starting BM25...");
            BM25Model rankModel3 = new BM25Model(path);
            rankedClassList = rankModel3.getRankedClasses(model);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("bm25");
            writer.append('\n');
            finalMap.get(query).put("bm25", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("bm25").add(tmpList);
            }
            writer.append('\n');
            map.clear();

            System.out.println("Starting VSM...");
            VectorSpaceModel rankModel4 = new VectorSpaceModel(path);
            rankedClassList = rankModel4.getRankedClasses(model, queryWords);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("vector-space");
            writer.append('\n');
            finalMap.get(query).put("vsm", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("vsm").add(tmpList);
            }

            writer.append('\n');
            map.clear();

            System.out.println("Starting PageRank...");
            PageRank rankModel8 = new PageRank(path);
            rankedClassList = rankModel8.getRankedClasses(model, queryWords);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("pagerank");
            writer.append('\n');
            finalMap.get(query).put("pagerank", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("pagerank").add(tmpList);
            }
            writer.append('\n');
            map.clear();

            System.out.println("Starting CMM...");

            ClassMatchMeasure rankModel5 = new ClassMatchMeasure(path);
            rankedClassList = rankModel5.getRankedClasses(model, queryWords,classMatchScoreMap);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("class-match-measure");
            writer.append('\n');
            finalMap.get(query).put("class-match-measure", new ArrayList<>());

            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("class-match-measure").add(tmpList);
            }
            writer.append('\n');
            map.clear();

            System.out.println("Starting SSM...");
            SemanticSimilarityMeasure rankModel7 = new SemanticSimilarityMeasure(path);
            rankedClassList = rankModel7.getRankedClasses(model, queryWords);
            map = getTopTen(rankedClassList);
            rankedClassList.clear();
            writer.append("semantic-similarity");
            writer.append('\n');
            finalMap.get(query).put("semantic-similarity", new ArrayList<>());
            for (int count = 0; count < map.get("iri").size(); count++) {
                writer.append(map.get("iri").get(count) + "\t");
                writer.append(map.get("label").get(count));
                writer.append('\n');
                writer.flush();
                List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("semantic-similarity").add(tmpList);
            }
            writer.append('\n');
            map.clear();


				/*System.out.println("Starting DM...");
				DensityMeasure rankModel6 = new DensityMeasure(path);
				rankedClassList = rankModel6.getRankedClasses(model, queryWords);
				map = getTopTen(rankedClassList, "DensityMeasure");
				rankedClassList.clear();
				writer.append("density-measure");
				writer.append('\n');
				finalMap.get(query).put("density-measure", new ArrayList<>());
				for (int count = 0; count < map.get("iri").size(); count++) {
					writer.append(map.get("iri").get(count) + "\t");
					writer.append(map.get("label").get(count));
					writer.append('\n');
					writer.flush();
			    List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("density-measure").add(tmpList);
				}
				writer.append('\n');
				map.clear();*/


				/*System.out.println("Starting BM...");
				BetweennessMeasure rankModel9 = new BetweennessMeasure(path);
				rankedClassList = rankModel9.getRankedClasses(model, queryWords);
				map = getTopTen(rankedClassList, "BetweennessMeasure");
				rankedClassList.clear();
				writer.append("between-measure");
				writer.append('\n');
				finalMap.get(query).put("between-measure", new ArrayList<>());
				for (int count = 0; count < map.get("iri").size(); count++) {
					writer.append(map.get("iri").get(count) + "\t");
					writer.append(map.get("label").get(count));
					writer.append('\n');
					writer.flush();
				List<String> tmpList = new ArrayList<>();
                tmpList.add(map.get("iri").get(count));
                tmpList.add(map.get("label").get(count));
                finalMap.get(query).get("between-measure").add(tmpList);
				}
				writer.append('\n');
				map.clear();*/

            /*System.out.println("Searching BioPortal, Solr and OLS...");
            SearchApps sa = new SearchApps();
            HashMap<String, LinkedList<SearchResult>> searchAppsResults = sa.search(query);
            for(String app : searchAppsResults.keySet()){
                writer.append(app+"\n");
                LinkedList<SearchResult> results = searchAppsResults.get(app);
                int i = 0;
                finalMap.get(query).put(app, new ArrayList<>());
                while(i < results.size() && i < 10){
                    writer.append(results.get(i).getIri()+ "\t");
                    writer.append(results.get(i).getLabel()+ "\n");
                    writer.flush();
                    List<String> tmpList = new ArrayList<>();
                    tmpList.add(results.get(i).getIri());
                    tmpList.add(results.get(i).getLabel());
                    finalMap.get(query).get(app).add(tmpList);
                    i++;
                }
                writer.append("\n");
            }*/
            writer.close();
        }
        return finalMap;
    }


    public static HashMap<String,ArrayList<String>> getTopTen (ArrayList<ResultFormatter> inputList){

        ArrayList<String> noDupsList = new ArrayList<>();
        ArrayList<ResultFormatter> list = new ArrayList<>();
        for(int i=0; i<inputList.size();i++){
            ResultFormatter searchFacet = (ResultFormatter)inputList.get(i);
            String iri = searchFacet.getTermIRI();
            if(!noDupsList.contains(iri)){
                noDupsList.add(iri);
                list.add(searchFacet);
            }

        }
        ArrayList<String> strings = new ArrayList<String>();
        ArrayList<String> labels = new ArrayList<>();
        HashMap<String,ArrayList<String>> map = new HashMap<>();

        int numberOfURIs = 10;
        if (list.size()<10) {
            numberOfURIs = list.size();
        }

        for(int j=0; j<numberOfURIs; j++){
            ResultFormatter searchFacet = (ResultFormatter)list.get(j);
            String term = searchFacet.getTermIRI();
            String label = searchFacet.getTermLabel();

            strings.add(term);
            labels.add(label);
        }
        map.put("iri",strings);
        map.put("label",labels);

        return map;
    }

}