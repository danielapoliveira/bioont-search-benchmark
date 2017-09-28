package rankingmodel.structuralMetrices;

import java.util.*;
import java.util.logging.Logger;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import store.QuadStore;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;

/**
 * Created by Anila Sahar Butt.
 */
public class BetweenessMeasureCal {

    public QuadStore store = QuadStore.getDefaultStore();
    public VirtGraph connection = store.getConnection();
    public Logger logger = Logger.getLogger(getClass().getName());

    private String path;

    public BetweenessMeasureCal(String path){
        this.path = path;
    }

    public void calculatePaths() {

        Paths pathClass = Paths.getDefaultMap(path);
        /********************  Get All graphs in the Result Set *****************************/
        ArrayList<String> graphs = this.getExistingLoadedOntology();

        for (int count=0; count<graphs.size() ; count++){
            double ssmSumForAllConcepts = 0.0;
            double k =0;
            double SSM=0.0;

            String graphIRI = graphs.get(count);
            logger.info(" /********** "+graphIRI +" *********/");

            ArrayList<String> classList = this.getClassList(graphIRI);

            HashMap<String, HashMap<String, ArrayList<String>>> ontPaths = new HashMap<String, HashMap<String, ArrayList<String>>>();

            int start = 0;

            for(int i=0; i<classList.size()-1 ; i++){
                if(i % 10 == 0){
                    System.out.println(i + " / " + classList.size() + " classes.");
                }
                HashMap<String, ArrayList<String>> listOfPaths = new HashMap<String, ArrayList<String>>();
                ArrayList<String> path =  new ArrayList<String>();
                String concept1 = classList.get(i);
                int commitSize;
                if(classList.size()>10000) {
                    commitSize = 10;
                } else{
                    commitSize = 100;
                }
                for (int j=i+1; j<classList.size() ; j++) {

                    String concept2 = classList.get(j);
                    try{
                        Tree tree1 = new Tree(concept1);
                        Tree tree2 = new Tree(concept2);
                        String commonNode = getMinimumPathCommonNode(concept1, concept2, graphIRI, tree1,tree2);

                        if (commonNode.equals("")){} else {
                            path = getPath(commonNode, tree1);

                            Collections.reverse(path);
                            path.add(commonNode);

                            ArrayList<String> path2 = getPath(commonNode, tree2);
                            path.addAll(path2);}


                    }catch(Exception e){
                    }

                    ///Store it now
                    listOfPaths.put(concept2, path);
                }

                ontPaths.put(concept1, listOfPaths);


                if (ontPaths.size() > commitSize) {
                    start++;
                    System.out.println("commit: " + start);
                    pathClass.save_adjacency_matrix_map(graphIRI, ontPaths);
                    pathClass.commit();
                    ontPaths = new HashMap<>();
                }


            }

            pathClass.save_adjacency_matrix_map(graphIRI, ontPaths);
            pathClass.commit();

        }

        pathClass.closeConnection();
    }



    public static ArrayList<String> getPath(String nodeIRI, Tree tree){

        ArrayList<String> list = new ArrayList<String>();

        Node node = tree.getNode(nodeIRI, tree.getRoot());

        while(!( node.getNode().equalsIgnoreCase(tree.getRoot().getNode()))){
            node = node.getNodeParent();
            String parentIRI = node.getNode();
            list.add(parentIRI);

        }

        return list;
    }


    public String getMinimumPathCommonNode(String concept1, String concept2, String graphIRI, Tree tree1, Tree tree2){

        double length = 1.0 ;



        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put(concept1, 0.0);
        map.put(concept2, 0.0);

        ArrayList<String> currentSuperClassesForConcept1 = getSuperClassList(concept1, graphIRI);
        ArrayList<String> currentSuperClassesForConcept2 = getSuperClassList(concept2, graphIRI);

        //add these lists to their respective maps along with their path length

        tree1.addChildren(currentSuperClassesForConcept1);
        tree2.addChildren(currentSuperClassesForConcept2);
        copyMap(map, currentSuperClassesForConcept1, length);
        copyMap(map, currentSuperClassesForConcept2, length);

        String matchedNode = intersection(tree1, tree2, map);

        while (matchedNode.equalsIgnoreCase("")){

            if(length<20) {
                length++;
                ArrayList<String> list1 = new ArrayList<String>();
                list1.addAll(currentSuperClassesForConcept1);

                ArrayList<String> list2 = new ArrayList<String>();
                list2.addAll(currentSuperClassesForConcept2);

                currentSuperClassesForConcept1.clear();
                currentSuperClassesForConcept2.clear();

                currentSuperClassesForConcept1 = getSuperClassListForList(list1, graphIRI, tree1);
                currentSuperClassesForConcept2 = getSuperClassListForList(list2, graphIRI, tree2);

                if(currentSuperClassesForConcept1.isEmpty() && currentSuperClassesForConcept2.isEmpty()){

                    break;
                } else {
                    copyMap(map, currentSuperClassesForConcept1, length);
                    copyMap(map, currentSuperClassesForConcept2, length);
                    matchedNode = intersection(tree1, tree2, map);
                }

            } else { matchedNode = "notFound";}
        }

        return matchedNode;
    }


    public String intersection(Tree tree1, Tree tree2, HashMap<String, Double> map){
        String matchedNode = "";

        for(Map.Entry<String, Double> entry: map.entrySet()){
            String nodeIRI = entry.getKey();
            if( (tree1.contains(nodeIRI, tree1.getRoot())) && (tree2.contains(nodeIRI, tree2.getRoot())) ){
                matchedNode = nodeIRI;
                break;
            }
        }

        return matchedNode;
    }

    public ArrayList<String> getSuperClassListForList(ArrayList<String> list, String graphIRI, Tree tree){

        ArrayList<String> superClassList = new ArrayList<String>();

        for (int i=0; i<list.size() ; i++){
            String nodeIRI = list.get(i);
            Node node = tree.getNode(nodeIRI, tree.getRoot());

            ArrayList<String> newList = new ArrayList<String>();

            newList = getSuperClassList(nodeIRI, graphIRI);

            for(int j=0; j<newList.size(); j++){
                superClassList.add(newList.get(j));

            }
            tree.addChildrenForNode(node,newList);
        }

        return superClassList;
    }

    public ArrayList<String> getClassList(String graphIRI){

        ArrayList<String> superClassList = new ArrayList<String>();

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                "SELECT ?class"
                + " FROM <"+graphIRI+"> "
                + " WHERE { {?class rdf:type rdfs:Class} UNION {?class rdf:type owl:Class.} "
                + " FILTER (!(isBlank(?class)) && (?class != owl:Thing))}";

        try {

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("class").toString();
                superClassList.add(graph);
            }
        } catch(Exception e){
            System.out.println(e);
        }
        return superClassList;
    }


    public ArrayList<String> getSuperClassList(String concept, String graphIRI){

        ArrayList<String> superClassList = new ArrayList<String>();

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                "SELECT  DISTINCT ?object"
                + " FROM <"+graphIRI+"> "
                + " WHERE {<"+concept+"> rdfs:subClassOf ?object."
                + "{?object rdf:type rdfs:Class} UNION {?object rdf:type owl:Class.}"
                + "FILTER (!(isBlank(?object)))}";
        try {

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                String graph = result.get("object").toString();
                superClassList.add(graph);
            }
        } catch(Exception e){
            System.out.println(e);
        }
        return superClassList;
    }


    public HashMap<String, Double> copyMap(HashMap<String, Double> map, ArrayList<String> list, double length) {

        for(int i=0; i<list.size();i++){
            if (map.containsKey(list.get(i))){
            } else {
                map.put(list.get(i), length);}
        }
        return map;
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

    public ArrayList<String> getExistingLoadedOntology(){

        ArrayList<String> list = new ArrayList<String>();

        String sparql = "SELECT DISTINCT ?graph "
                + " WHERE {GRAPH ?graph {?subject ?property ?object}} ORDER BY (?graph)";

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);

        ResultSet results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.nextSolution();
            String graph = result.get("graph").toString();
            list.add(graph);
        }

        return list;
    }

}
