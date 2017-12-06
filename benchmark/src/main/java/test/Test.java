package test;

import dataload.LoadDataClass;
import evaluation.Evaluation;
import evaluation.Evaluation_gt;
import store.QuadStore;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Daniela Oliveira.
 */
public class Test {
    //Choose to run the full framework or just part of it.
    private static boolean loadData = false;
    private static boolean preprocess = true;
    private static boolean search = true;
    private static boolean evaluate = true;
    private static boolean evaluateGtOnly = false;

    //Choose precalculations to execute.
    private static boolean tfidf = false;
    private static boolean bm25 = true;
    private static boolean vsm = true;
    private static boolean pageRank = true;
    private static boolean bm = false;
    private static boolean dm = false;

    private static int k = 5;

    private static void createDir(String dirName){
        File dir = new File(dirName);

        if(!dir.exists()){
            boolean result = false;

            try{
                dir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                System.out.println("Couldn't create directory because "+ se);
            }
            if(result) {
                System.out.println(dirName + " directory created created.");
            }
        }
    }

    public static void main(String[] args) throws Exception {


        String urisFile = Configuration.getProperty(Configuration.URIS_FILENAME);
        System.out.println(urisFile);
        String queriesFile = Configuration.getProperty(Configuration.QUERY_FILENAME);
        String savePath = Configuration.getProperty(Configuration.SAVE_PATH);

        String preprocessingPath = savePath + "ranking_models/";
        createDir(preprocessingPath);
        String resultsPath = savePath + "ranking_results/";
        createDir(resultsPath);
        String evaluationPath = savePath + "evaluation/";
        createDir(evaluationPath);


        /********************
         * Load data        *
         *******************/
        if (loadData) {
            QuadStore qd = QuadStore.getDefaultStore();
            ArrayList<String> store = qd.getExistingLoadedOntology();
            LoadDataClass ldc = new LoadDataClass(preprocessingPath);
            ldc.loadMetaData(store);
        }

        /********************
         * Calculations     *
         *******************/
        if(preprocess) {
            DoCalculationsParallel calc = new DoCalculationsParallel(preprocessingPath);
            calc.calculate(tfidf, bm25, vsm, pageRank, bm, dm);
        }

        /********************
         * Search           *
         *******************/
        HashMap<String,HashMap<String,List<List<String>>>> resultsMap = new HashMap<>();
        if(search) {
            CalculateRankings results = new CalculateRankings(preprocessingPath);
            resultsMap = results.search(queriesFile, resultsPath);
        }


        /********************
         * Evaluation       *
         *******************/
        if(evaluate){
            Evaluation eval = new Evaluation(evaluationPath);
            eval.calculate(resultsMap, Configuration.getProperty(Configuration.GROUND_TRUTH_PATH),k);

        }

        if(evaluateGtOnly){
            Evaluation_gt eval_gt = new Evaluation_gt(evaluationPath);
            eval_gt.calculate(resultsMap, k);
        }


    }
}
