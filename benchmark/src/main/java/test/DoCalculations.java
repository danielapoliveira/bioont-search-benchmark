package test;

import rankingmodel.bm25.BM25Calculator;
import rankingmodel.pageRank.AdjacencyMatrixComputations;
import rankingmodel.pageRank.PageRank;
import rankingmodel.pageRank.PageRankTF_IDFMap;
import rankingmodel.structuralMetrices.BetweenessMeasureCal;
import rankingmodel.structuralMetrices.DensityCalculator;
import rankingmodel.tf_Idf.TF_IDFCalculator;

/**
 * Created by Daniela Oliveira.
 */
public class DoCalculations {

    private String path;
    public DoCalculations(String path){
        this.path = path;

    }

    /**
     * Class that preprocesses for every algorithm chosen for the ontologies loaded in the Virtuoso database.
     */
    public void calculate(boolean tfidf, boolean bm25, boolean pageRank, boolean bm, boolean dm){
        if(tfidf) {
            TF_IDFCalculator tc = new TF_IDFCalculator(path);
            tc.tf_IdfCalculations();
        }

        if(bm25) {
            BM25Calculator calc = new BM25Calculator(path);
            calc.calculateOntologyTermStatistics();
        }

        if(pageRank) {
            PageRankTF_IDFMap prtf = new PageRankTF_IDFMap(path);
            prtf.initializePageRankScore();
            prtf.initializePageRankScoreForOntologyGraphs();

            AdjacencyMatrixComputations am = new AdjacencyMatrixComputations(path);
            am.createOutLinkMap();
            am.createAdjacencyMatrix();

            PageRank pr = new PageRank(path);
            pr.calculatePageRank();
        }

        if(bm) {
            BetweenessMeasureCal bmc = new BetweenessMeasureCal(path);
            bmc.calculatePaths();
        }

        if(dm) {
            DensityCalculator dc = new DensityCalculator(path);
            dc.saveDensityforCorpus();
        }
    }
}
