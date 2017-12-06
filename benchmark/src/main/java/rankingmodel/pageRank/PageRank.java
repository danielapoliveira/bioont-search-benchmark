package rankingmodel.pageRank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.*;
import jdbm.PrimaryTreeMap;
import query.ResultFormatter;
import query.TF_IDFHolder;
import rankingmodel.tf_Idf.TfIdf_Data;


/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class PageRank {

    private static double damping_factor = 0.85;
    private long total_num_nodes= 0;
    private double threshold=0.01;

    private Logger logger;
    private PageRankMap initialMapClass;
    private DomainAdjacencyMatrix adjacency_matrix_class;
    private PrimaryTreeMap<String, Double> rank_value_table;
    private PrimaryTreeMap<String, ArrayList<String>> adjacency_matrix;
    private OutLinksMap outlinkCount;
    private PrimaryTreeMap<String, Integer> outlinks ;
    private TfIdf_Data tfIdfClass;
    private PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map;

    private String path;

    public PageRank(String path){
        this.path = path;
        tfIdfClass= TfIdf_Data.getDefaultMap(path);
        logger = Logger.getLogger(getClass().getName());
        corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();

        initialMapClass =  PageRankMap.getDefaultMap(path);
        rank_value_table = initialMapClass.get_tf_Idf_map();

        adjacency_matrix_class  = DomainAdjacencyMatrix.getDefaultMap(path);
        adjacency_matrix =  adjacency_matrix_class.get_domain_adjacency_matrix_map();

        outlinkCount = OutLinksMap.getDefaultMap(path);
        outlinks = outlinkCount.get_outlinks_map();

        total_num_nodes = initialMapClass.getTotalNumberOfNodes();

        //logger.info(" total_num_nodes : " + total_num_nodes);
    }

    public ArrayList<ResultFormatter> getRankedClasses(Model model, ArrayList<String> queryWords){


        ArrayList<ResultFormatter> resultList = new ArrayList<ResultFormatter>();
        HashMap<String, Double> tf_IdfMap = new HashMap<String, Double>();

        TfIdf_Data tfIdfClass = TfIdf_Data.getDefaultMap(path);
        PrimaryTreeMap<String, HashMap<String, HashMap<Integer,Double>>> corpus_tfIdf_Map = tfIdfClass.get_tfIdf_Value();

        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

        ResIterator resourceIterator = model.listSubjectsWithProperty(label);

        while(resourceIterator.hasNext()){
            Resource uri = resourceIterator.nextResource();
            NodeIterator nodeIterator = model.listObjectsOfProperty(uri, graphProperty);
            while(nodeIterator.hasNext()){
                double tfIdf=0;
                String node = nodeIterator.nextNode().toString();
                double graphRank = rank_value_table.get(node);
                if (corpus_tfIdf_Map.containsKey(node)) {
                    HashMap<String, HashMap<Integer,Double>> ontologyTfIDFs = corpus_tfIdf_Map.get(node);
                    if(ontologyTfIDFs.containsKey(uri.toString())) {
                        HashMap<Integer,Double> tfIdfs = ontologyTfIDFs.get(uri.toString());
                        tfIdf = tfIdfs.get(3);
                    } else { }
                } else { }

                if (tf_IdfMap.containsKey(uri.toString())){
                    double value = tf_IdfMap.get(uri.toString());
                    if (value < tfIdf){
                        tf_IdfMap.put(uri.toString(), tfIdf+ graphRank);
                    }
                } else {
                    tf_IdfMap.put(uri.toString(), tfIdf+ graphRank);
                }
            }
        }
        /*************************** Sort Hash map for tf_idf score **********************************/

        HashMap<String, Double> sortedtf_IdfMap = sortByValues(tf_IdfMap);

        /*********************************************************************************************/

        for (Map.Entry<String, Double> entry : sortedtf_IdfMap.entrySet()) {

            ResultFormatter result = new ResultFormatter();
            String term = entry.getKey();
            result.setTermIRI(term);
            result.setGraphIRI(entry.getKey().toString());
            result.setTermLabel(getLabel(model.listObjectsOfProperty(model.createResource(term), label), term));
            result.setScore(entry.getValue().toString());
            resultList.add(result);
        }
        return resultList;
    }

    private TF_IDFHolder getTF_IDFValues(String term, String graphIRI){

        TF_IDFHolder tf_IdfHolder = new TF_IDFHolder();

        double tf = 0;
        double idf = 0;
        double tf_idf = 0;

        if (corpus_tfIdf_Map.containsKey(graphIRI)) {
            HashMap<String, HashMap<Integer,Double>> ontologyTfIDFs = corpus_tfIdf_Map.get(graphIRI);
            if(ontologyTfIDFs.containsKey(term)) {
                HashMap<Integer,Double> tfIdfs = ontologyTfIDFs.get(term);
                tf = tfIdfs.get(1);
                idf = tfIdfs.get(2);
                tf_idf = tfIdfs.get(3);
            }
        }

        tf_IdfHolder = new TF_IDFHolder(tf, idf, tf_idf);
        return tf_IdfHolder;
    }
    public void calculatePageRank() {


        /******************************************************************************
         * Get Total number of nodes in corpus
         * ****************************************************************************/
        PageRankMap initialMapClass =  PageRankMap.getDefaultMap(path);
        total_num_nodes = initialMapClass.getTotalNumberOfNodes();


        /******************************************************************************
         * Algorithm execution. It stops execution once convergence has occurred for given corpus
         * ****************************************************************************/
        int iteration=50;
        while(iteration>0){
            System.out.append("\rPRC: "+(50-iteration)*100/50+"%").flush();
            iteration--;
            //logger.info(" this is iteration number  : " + iteration);
			/*Following code store the stale value of page rank in a hashmap rank_values_table before it calls the function join_rvt_am. 
			 * These values are then compared against new page rank values in rank_values_table after this function has finished execution
			*/
            double stale_pagerank_sum = 0.0;
            double current_pagerank_sum= 0.0;

            for(Map.Entry<String, Double> entry: rank_value_table.entrySet()) {
                stale_pagerank_sum+=entry.getValue();
            }

            //logger.info(" stale_pagerank_sum : " + stale_pagerank_sum);
            join_rvt_am();

			/* Loop calculates difference between current and old page rank value for each URL.
			 * Sum of difference of all the URLs is compared against predefined Threshold value. 
			 * When this difference goes below threshold, Loop breaks out */

            for(Map.Entry<String, Double> entry: rank_value_table.entrySet()) {
                current_pagerank_sum+=entry.getValue();
            }
            //logger.info(" current_pagerank_sum : " + current_pagerank_sum);

            double difference = current_pagerank_sum - stale_pagerank_sum;


            //logger.info(" difference : " + difference);
            if(difference<threshold) {
                break;
            }	else {

            }
        }
    }

    /*************************************************************************
     * Function to calculate page rank value for the list of all input URLs
     * This is an iterative process until convergence.
     * Once convergence occurs, that is there is no difference between current
     * and earlier values of page rank, this method will no longer be called
     * ************************************************************************/

    public void join_rvt_am(){


        ArrayList<String> inlink_list = new ArrayList<String>();

        LinkedHashMap<String,Double> intermediate_rvt = new LinkedHashMap<String,Double>();
		
		/*intermediate_rvt_iter hashmap stores pagerank values for dangling nodes 
		 * along with page rank values of its successor nodes weighed by their outbound links */

        for(Map.Entry<String, Double> entry: rank_value_table.entrySet()) {
            String graphIRI = entry.getKey();
            intermediate_rvt.put(graphIRI, 0.0);
        }

        String source_url="";
        int inlinkCount=0;
        String inlink_url="";
        double intermediate_rank_value=0.0;
        double dangling_value=0;
        double dangling_value_per_page=0.0;

		/* Iterate over all the URLs in given input file */
        Iterator<String> ite=adjacency_matrix.keySet().iterator();

        while(ite.hasNext()) {

            source_url= ite.next();
            inlink_list=adjacency_matrix.get(source_url);
            inlinkCount=inlink_list.size();

			/* Assign updated page rank value to all the successors of current node 
			 * Page rank value is equal to page rank value of predecessor node 
			 * weighed by the number of its outbound links */

            for(int i=0;i<inlinkCount;i++)	{
                inlink_url=inlink_list.get(i);
                intermediate_rank_value = 	intermediate_rank_value + (rank_value_table.get(inlink_url)/ (outlinks.get(inlink_url)));
                intermediate_rvt.put(source_url,intermediate_rank_value);
            }
            //System.out.println(rank_value_table);
			/* Special case for dangling node with no outbound link */
            //System.out.println(source_url);
            if(outlinks.get(source_url)==0 ) {
                dangling_value+=rank_value_table.get(source_url);
            }
        }

        dangling_value_per_page= dangling_value /(double)total_num_nodes;

        //logger.info("dangling_value_per_page " + dangling_value_per_page);
		
		/*Page rank of all the dangling links is calculated and is distributed among all the webpages to minimize effect of dangling nodes.
		 * Without this facility, average page rank of given graph will be less than 1 with poor utilization */

        for(Map.Entry<String, Double> entry: rank_value_table.entrySet()) {
            String key = entry.getKey();
            double value = intermediate_rvt.get(key) + dangling_value_per_page;
            intermediate_rvt.put(key, value);
        }

        //System.out.println(" intermediate_rvt value updated : ");

		/*Final page rank value for given page for given number of iteration. 
		 * Page rank is calculated by considering two scenarios, first is booleanModel surfer
		 * model and second is the possibility that surfer might reach particular page
		 * after clicking specific number of URLs in the given pool */

        for(Map.Entry<String, Double> entry: rank_value_table.entrySet()) {
            double value =damping_factor*intermediate_rvt.get(entry.getKey())+((1-damping_factor)*((1.0)/(double)total_num_nodes));
            initialMapClass.save_tf_Idf_map(entry.getKey(),value);
        }

    }




    private String getLabel(NodeIterator labelIterator, String propertyURI){
        String propLabel="";

        if (labelIterator.hasNext()){
            RDFNode pLabel = labelIterator.nextNode();
            propLabel = pLabel.toString();
            if(propLabel.contains("@")) {
                propLabel=propLabel.split("@")[0];
            }
            if (propLabel.contains("^")){
                propLabel= propLabel.split("\\^")[0];
            }
        } else {
            propLabel = propertyURI;
        }
        return propLabel;
    }


    private HashMap<String, Double> sortByValues(HashMap<String, Double> map) {
        List list = new ArrayList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey().toString(), Double.parseDouble(entry.getValue().toString()));
        }
        return sortedHashMap;
    }
}
