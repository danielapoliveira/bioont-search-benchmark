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
        api.setURL("http://www.ebi.ac.uk/ols/beta/api/");
        JsonNode searchResult = api.jsonToNode(api.get(api.getUrl() + "search?q=" + term + "&rows=25")).get("response").get("docs");
        for(JsonNode result : searchResult)
        {
            SearchResult s = new SearchResult();
            String purl = result.get("iri").toString().replace("\"", "");
            s.setIri(purl);

            String acronym = result.get("ontology_prefix").toString().replace("\"", "");
            s.setAcronym(acronym);

            if(result.get("description") != null)
                s.setDefinition(result.get("description").toString().replace("\"", ""));

            s.setLabel(result.get("label").toString().replace("\"", ""));
            if(validAcronyms.contains(acronym))
                search.add(s);

        }

        return search;
    }

    public LinkedList<SearchResult> bioportalSearch(String term, List<String> validAcronyms) {

        LinkedList<SearchResult> search = new LinkedList<>();
        api.setURLandKey("http://data.bioontology.org", Configuration.getProperty(Configuration.BIOPORTAL_APIKEY));
        JsonNode searchResult = api.jsonToNode(api.get(api.getUrl() + "/search?q=" + term.replace("+","+"))+ "&pagesize=30").get("collection");

        for(JsonNode result : searchResult) {
            
            SearchResult s = new SearchResult();
            String purl = result.get("@id").toString().replace("\"", "");
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
            if(validAcronyms.contains(acronym))
                search.add(s);

        }
        return search;
    }

}
