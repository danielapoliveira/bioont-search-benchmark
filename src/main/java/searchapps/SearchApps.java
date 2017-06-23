package searchapps;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniela Oliveira.
 */
public class SearchApps {

    public  HashMap<String, LinkedList<SearchResult>> search(String term) throws Exception {

        List<String> validAcronyms = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("userinput/acronyms.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            validAcronyms.add(line.trim());
        }
        HashMap<String, LinkedList<SearchResult>> allResults = new HashMap<>();

        term = term.replace(" ","+").trim();

        //search solr
        SolrSearch s = new SolrSearch();
        LinkedList<SearchResult> results = s.search(term, "label:");

        SolrSearch syn = new SolrSearch();
        results.addAll(syn.search(term, "synonym:"));

        SolrSearch defSearch = new SolrSearch();
        results.addAll(defSearch.search(term, "description:"));

        //Sort Solr results in descending order
        Collections.sort(results, (p0, p1) -> p0.getScore().compareTo(p1.getScore()));
        Collections.sort(results, Collections.reverseOrder());
        LinkedList<SearchResult> mergedResults = removeDups(results);

        // all ontologies used in the current search
        Set<String> acroUsed = s.getAcronyms();
        acroUsed.addAll(syn.getAcronyms());
        allResults.put("solr", mergedResults);

        //Bioportal search
        Search bio = new Search();
        LinkedList<SearchResult> bioResults = new LinkedList<>();
        int tries = 10;
        for (int j = 0; j <= tries; j++) {
            try {
                bioResults = bio.bioportalSearch(term, validAcronyms);
                break;
            } catch (NullPointerException e) {
                System.out.println("Bioportal timeout and retry...");
                Thread.sleep(TimeUnit.MINUTES.toMillis(15));
            }
            if (j == 10)
                System.exit(-1);
        }
        bioResults = removeDups(bioResults);
        allResults.put("bioportal", bioResults);

        //OLS search
        Search ols = new Search();
        LinkedList<SearchResult> olsResults = new LinkedList<>();
        for (int j = 0; j <= tries; j++) {
            try {
                olsResults = ols.olsSearch(term, validAcronyms);
                break;
            } catch (NullPointerException e) {
                System.out.println("OLS timeout and retry...");
                Thread.sleep(TimeUnit.MINUTES.toMillis(15));
            }
            if (j == 10)
                System.exit(-1);
        }
        olsResults = removeDups(olsResults);
        allResults.put("ols", olsResults);


        return allResults;
    }

    private static LinkedList<SearchResult> removeDups(LinkedList<SearchResult> r) {
        LinkedList<SearchResult> noDups = new LinkedList<>();
        List<String> tmpList = new ArrayList<>();
        for(SearchResult s : r) {
            if(!tmpList.contains(s.getIri())) {
                noDups.add(s);
                tmpList.add(s.getIri());
            }
        }
        return noDups;
    }

    private static File[] getFileList(String dirPath) {
        File dir = new File(dirPath);
        return dir.listFiles();
    }

}
