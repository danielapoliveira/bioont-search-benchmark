package rankingmodel.tf_Idf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.hp.hpl.jena.rdf.model.*;
import jdbm.PrimaryTreeMap;

import query.ResultFormatter;


/**
 * Created by Anila Sahar Butt. Modified by Daniela Oliveira.
 */
public class TF_IDFModel {

    private String path;

    public TF_IDFModel(String path){
        this.path = path;
    }

    public ArrayList<ResultFormatter> getRankedClasses(Model model) {

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
                        tf_IdfMap.put(uri.toString(), tfIdf);
                    }
                } else {
                    tf_IdfMap.put(uri.toString(), tfIdf);
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
            result.setGraphIRI("");
            result.setTermLabel(getLabel(model.listObjectsOfProperty(model.createResource(term), label), term));
            result.setScore(entry.getValue().toString());
            resultList.add(result);
        }

        return resultList;
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
