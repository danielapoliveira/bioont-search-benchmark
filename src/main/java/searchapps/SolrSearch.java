package searchapps;

import test.Configuration;
import settings.FieldWeight;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Daniela Oliveira.
 */
public class SolrSearch {

    LinkedList<SearchResult> results;


    public SolrSearch() {
        results = new LinkedList<>();
    }

    public LinkedList<SearchResult> search(String term, String q){
        //String solrUrl = "http://localhost:8983/solr/ontology";
        SolrClient solrServer = new HttpSolrClient(Configuration.getProperty(Configuration.SOLR_INSTANCE));
        SolrQuery query = new SolrQuery();
        query.setFields("iri","label","synonym","ontology_prefix","score","description");
        query.set("q",q+term);

        QueryResponse response = null;
        try {
            response = solrServer.query(query);

        } catch (SolrServerException e) {/* */ } catch (IOException e) {
            e.printStackTrace();
        }
        SolrDocumentList list = response.getResults();

        for(SolrDocument item : list) {
            SearchResult s = new SearchResult();
            s.setAcronym(item.get("ontology_prefix").toString());
            s.setIri(item.get("iri").toString());
            s.setScore(item.get("score").toString());
            s.setLabel(item.get("label").toString());
            try {
                s.setDefinition(item.get("description").toString());
            } catch (NullPointerException e){

            }
            if(q.equals("label:")){
                s.setWeight(FieldWeight.LABEL);
            }
            else if (q.equals("synonym:")){
                s.setWeight(FieldWeight.EXACT_SYNONYM);
            }
            else
                s.setWeight(FieldWeight.DEFINITION);

            results.add(s);
        }
        return results;
    }

    public Set<String> getAcronyms(){
        Set<String> acros = new TreeSet<>();
        for(SearchResult s : results){
            acros.add(s.getAcronym());
        }
        return acros;
    }
}
