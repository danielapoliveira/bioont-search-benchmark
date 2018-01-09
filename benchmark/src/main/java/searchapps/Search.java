package searchapps;

import test.Configuration;
import settings.ApiHandler;
import settings.FieldWeight;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Daniela
 */
public class Search {
    //Attributes
    private ApiHandler api;
    private HashMap<String, Set<String>> searchResults;


    /**
     * Constructor
     */
    public Search() {
        api = new ApiHandler();
        searchResults = new HashMap<>();
    }


    public Set<String> getAcronyms() {
        return searchResults.keySet();
    }

    public LinkedList<SearchResult> olsSearch(String term,List<String> validAcronyms){
        LinkedList<SearchResult> search = new LinkedList<>();
        api.setURL("https://www.ebi.ac.uk/ols/api/");
        JsonNode searchResult = api.jsonToNode(api.get(api.getUrl() + "search?q=" + term.toLowerCase() + "&rows=25&exact=true")).get("response").get("docs");
		if(searchResult != null){
        for(JsonNode result : searchResult)
        {
            SearchResult s = new SearchResult();
            //System.out.println(result);
            String purl = result.get("iri").toString().replace("\"", "");
            s.setIri(purl);

            String acronym = result.get("ontology_prefix").toString().replace("\"", "");
            s.setAcronym(acronym);

            if(result.get("description") != null)
                s.setDefinition(result.get("description").toString().replace("\"", ""));

            s.setLabel(result.get("label").toString().replace("\"", ""));
            //String acroFromUri = getOntologyFromUri(purl);
			//if (acroFromUri.matches("[0-9]+"))
				//acroFromUri = acronym;
			
			//System.out.println(acronym);
			//System.out.println(validAcronyms.contains(acronym));
            if(validAcronyms.contains(acronym)){
				
                search.add(s);
		}

        }
		}
        return search;
    }

    public LinkedList<SearchResult> zoomaSearch(String term,List<String> validAcronyms){
        //System.out.println(term);
        LinkedList<SearchResult> search = new LinkedList<>();
        api.setURL("https://www.ebi.ac.uk/spot/zooma/v2/api/");
        JsonNode searchResult = api.jsonToNode(api.get(api.getUrl() + "services/annotate?propertyValue=" + term.toLowerCase()));
		if(searchResult != null){
        for(JsonNode result : searchResult)
        {
            SearchResult s = new SearchResult();
            String purl = result.get("semanticTags").toString().replace("[", "").replace("]", "").replace("\"", "");

            s.setIri(purl);
            s.setLabel(result.get("annotatedProperty").get("propertyValue").toString().replace("\"", ""));
            //String acroFromUri = getOntologyFromUri(purl);
            //if(validAcronyms.contains(acronym))
                search.add(s);
        }
		}
        return search;
    }

    public LinkedList<SearchResult> bioportalSearch(String term, List<String> validAcronyms) {

        LinkedList<SearchResult> search = new LinkedList<>();
        api.setURLandKey("http://data.bioontology.org", Configuration.getProperty(Configuration.BIOPORTAL_APIKEY));
        JsonNode searchResult = api.jsonToNode(api.get(api.getUrl() + "/search?q=" + term.replace("+","+")+ "&pagesize=100&require_exact_match=true")).get("collection");
		if(searchResult != null){
        for(JsonNode result : searchResult) {
            SearchResult s = new SearchResult();
            String purl = result.get("@id").toString().replace("\"", "");
			//System.out.println(purl);
            if(purl.contains("http://purl.org/sig/ont/fma/fma"))
                purl = purl.replace("http://purl.org/sig/ont/fma/fma","http://purl.obolibrary.org/obo/FMA_");
            if(purl.contains("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#")) {
                purl = purl.replace("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#", "http://purl.obolibrary.org/obo/NCIT_");
            }
            s.setIri(purl);
            if(result.get("prefLabel") != null)
                s.setLabel(result.get("prefLabel").toString().replace("\"", ""));
            if(result.get("definition") != null)
                s.setDefinition(result.get("definition").toString().replace("\"", ""));
            String matchType = result.get("matchType").toString().replace("\"", "");
            if(matchType.equals("prefLabel"))
                s.setWeight(FieldWeight.LABEL);
            else
                s.setWeight(FieldWeight.EXACT_SYNONYM);

            String ontId = result.get("links").get("ontology").toString().replace("\"", "");

            String acronym = api.jsonToNode(api.get(ontId)).get("acronym").toString().replace("\"", "").toUpperCase();
            s.setAcronym(acronym);
            //String acroFromUri = getOntologyFromUri(purl);
            if(validAcronyms.contains(acronym))
                search.add(s);

        }
		}
        return search;
    }

    private String getOntologyFromUri(String uri){
        String[] tmpSplit = uri.split("/");
        String ontology = tmpSplit[(tmpSplit.length-1)].split("_")[0];
        return ontology;
    }

}
