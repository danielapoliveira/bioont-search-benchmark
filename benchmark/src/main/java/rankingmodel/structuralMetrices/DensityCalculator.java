package rankingmodel.structuralMetrices;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rankingmodel.tf_Idf.TF_IDFQueryAnalyzer;
import store.QuadStore;

import jdbm.PrimaryTreeMap;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 * Created by Anila Sahar Butt.
 */
public class DensityCalculator {

    QuadStore store;
    private VirtGraph connection;
    private Logger logger;
    private String path;
    HashMap<String, HashMap<String, Integer>> map;

    public DensityCalculator(String path){
        this.path = path;

        store = QuadStore.getDefaultStore();
        connection = store.getConnection();
        logger = Logger.getLogger(getClass().getName());
        map = new HashMap<>();

    }
    public void saveDensityforCorpus(){

        ArrayList<String> ontologies= new ArrayList<String>();

        TF_IDFQueryAnalyzer query_analyzer= new TF_IDFQueryAnalyzer();
        ontologies = query_analyzer.getExistingLoadedOntology();

        DensityMap densityMap =DensityMap.getDefaultMap(path);

        for(int i=0;i<ontologies.size();i++)	{

            String graphIRI = ontologies.get(i);
            logger.info(" For Graph " + i +" / " + ontologies.size()  + " --> " + graphIRI );

            HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();

            ArrayList<String> classList = this.getClassList(graphIRI);
            for (int j=0; j<classList.size() ; j++) {
                String classURI = classList.get(j);
                HashMap<String, Integer> classDensiy = this.calculateDensityForGraph(classURI, graphIRI);
                map.put(classURI, classDensiy);
            }

            //logger.info(map.toString());
            densityMap.save_density_map(graphIRI, map);
        }

        densityMap.closeConnection();
    }

    private String getLocalName(String uri)
    {
        int index = uri.indexOf("#") + 1;
        if(index == 0)
            index = uri.lastIndexOf("/") + 1;
        int lastIndex = uri.indexOf(">");
        if(index == 0 || index > lastIndex)
            return "";
        return uri.substring(index,lastIndex);
    }

    private int getAllSubclasses(OWLClass c, OWLOntology o, int prevSize, int count){

        List<OWLClassExpression> subList = EntitySearcher.getSubClasses(c,o).collect(Collectors.toList());
        int size = subList.size();

        if(prevSize > 0 && size == 0){
            return count;
        } else {

            for(OWLClassExpression ow : subList) {
                count += 1;
                count = getAllSubclasses(ow.asOWLClass(), o, size, count);
            }

        }

        return count;
    }

    private int getAllSuperclasses(OWLClass c, OWLOntology o, int prevSize, int count){
        List<OWLClassExpression> subList = EntitySearcher.getSuperClasses(c,o).collect(Collectors.toList());
        int size = subList.size();
        if(size == 0){
            return count;
        } else {
            OWLClassExpression ow = subList.get(0);
            try {
                OWLClass cls = ow.asOWLClass();
                count += 1;
                count = getAllSubclasses(cls, o, size, count);
            } catch(Exception e){}
        }
        return count;
    }

    public HashMap<String, Integer> getSiblingsAndProps(String classURI,String graphIRI){
        HashMap<String, Integer> list = new HashMap<String, Integer>();
        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT (count(DISTINCT ?superCl) as ?superClasses) (count(DISTINCT ?restriction) as ?relations) (count(DISTINCT ?others) as ?siblings)"
                + "FROM <"+graphIRI+">"
                + "WHERE {<"+classURI+"> rdf:type ?classType."
                + "OPTIONAL {<"+classURI+"> rdfs:subClassOf+ ?superCl.?superCl rdf:type ?classType.}"
                + "OPTIONAL {<"+classURI+"> rdfs:subClassOf ?someCl. ?others rdfs:subClassOf ?someCl."
                + "Filter (?others != <"+classURI+">). "
                + "?others rdf:type ?classType. }"
                + "OPTIONAL {{{?restriction rdfs:domain <"+classURI+">}union {?restriction rdfs:domain ?intermediateClass. <"+classURI+"> rdfs:subClassOf+ ?intermediateClass}. ?restriction rdf:type rdf:Property} union {<"+classURI+"> rdfs:subClassOf+ ?restriction.?restriction rdf:type owl:Restriction.}}"
                + "FILTER (((?classType = owl:Class) || (?classType = rdfs:Class)) && !(isBlank(?class) ))}";


        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);
        try {
            ResultSet results = vqe.execSelect();
            while (results.hasNext()){

                QuerySolution qs = results.nextSolution();
                int relations=0;
                relations= Integer.parseInt(qs.getLiteral("relations").toString().split("\\^")[0]);
                int siblings=0;
                siblings=Integer.parseInt(qs.getLiteral("siblings").toString().split("\\^")[0]);

                list.put("relations", relations);
                list.put("siblings", siblings);
            }
        } catch(Exception e){
            logger.info("can't calculate density for graph because :" + e);
        } finally {
            vqe.close();
        }
        return list;
    }

    private String getLabel(OWLClass c, OWLOntology o){
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLAnnotationProperty label = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        String labelVal = "";
        //Get Labels
        List<OWLAnnotation> annots = EntitySearcher.getAnnotationObjects(c, o,label).collect(Collectors.toList());
        for (OWLAnnotation annotation : annots){

            if (annotation.getValue() instanceof OWLLiteral) {
                OWLLiteral val = (OWLLiteral) annotation.getValue();
                labelVal = val.toString();
                break;
            }
        }
        return labelVal;
    }


    public HashMap<String, Integer> calculateDensityForGraph(String classURI,String graphIRI){
        HashMap<String, Integer> list = new HashMap<String, Integer>();

        String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "SELECT (count(DISTINCT ?subCl) as ?subClasses) (count(DISTINCT ?superCl) as ?superClasses) (count(DISTINCT ?restriction) as ?relations) (count(DISTINCT ?others) as ?siblings)"
                + "FROM <"+graphIRI+">"
                + "WHERE {<"+classURI+"> rdf:type ?classType."
                + "OPTIONAL {?subCl rdfs:subClassOf+ <"+classURI+">. ?subCl rdf:type ?classType.}"
                + "OPTIONAL {<"+classURI+"> rdfs:subClassOf+ ?superCl.?superCl rdf:type ?classType.}"
                + "OPTIONAL {<"+classURI+"> rdfs:subClassOf ?someCl. ?others rdfs:subClassOf ?someCl."
                + "Filter (?others != <"+classURI+">). "
                + "?others rdf:type ?classType. }"
                + "OPTIONAL {{{?restriction rdfs:domain <"+classURI+">}union {?restriction rdfs:domain ?intermediateClass. <"+classURI+"> rdfs:subClassOf+ ?intermediateClass}. ?restriction rdf:type rdf:Property} union {<"+classURI+"> rdfs:subClassOf+ ?restriction.?restriction rdf:type owl:Restriction.}}"
                + "FILTER (((?classType = owl:Class) || (?classType = rdfs:Class)) && !(isBlank(?class) ))}";


        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, connection);


        try {
            ResultSet results = vqe.execSelect();
            while (results.hasNext()){

                QuerySolution qs = results.nextSolution();
                int subClasses= 0;
                subClasses=Integer.parseInt(qs.getLiteral("subClasses").toString().split("\\^")[0]);
                int superClasses= 0;
                superClasses=Integer.parseInt(qs.getLiteral("superClasses").toString().split("\\^")[0]);
                int relations=0;
                relations= Integer.parseInt(qs.getLiteral("relations").toString().split("\\^")[0]);
                int siblings=0;
                siblings=Integer.parseInt(qs.getLiteral("siblings").toString().split("\\^")[0]);

                list.put("subClasses", subClasses);
                list.put("superClasses", superClasses);
                list.put("relations", relations);
                list.put("siblings", siblings);

            }
        } catch(Exception e){
            logger.info("can't calculate density for graph because :" + e);
        } finally {
            vqe.close();
        }

        return list;
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

    public HashMap<String,HashMap<String,Integer>> getOntologyDensityGraph(String docId){

        System.out.println(" **** getOntologyDensityGraph ****");
        HashMap<String,HashMap<String,Integer>> ontologyDensityMap = new HashMap<String,HashMap<String,Integer>>();
        DensityMap densityMapObject = DensityMap.getDefaultMap(path);
        PrimaryTreeMap<String, HashMap<String,HashMap<String,Integer>>> map = densityMapObject.get_density_map();
        ontologyDensityMap = map.get(docId);

        return ontologyDensityMap;
    }
}
