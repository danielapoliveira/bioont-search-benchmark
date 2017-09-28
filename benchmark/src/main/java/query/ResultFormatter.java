package query;

/**
 * Created by Anila Sahar Butt.
 */
public class ResultFormatter {

    private String graphIRI="";
    private String termIRI="";
    private String termLabel="";
    private String score="";

    public void setGraphIRI(String graph){
        this.graphIRI = graph;
    }

    public void setTermIRI(String term){
        this.termIRI = term;
    }

    public void setTermLabel(String termLabel){
        this.termLabel = termLabel;
    }

    public void setScore(String score){
        this.score = score;
    }


    public String getGraphIRI(){
        return this.graphIRI;
    }

    public String getTermIRI(){
        return this.termIRI;
    }

    public String getTermLabel(){
        return this.termLabel;
    }

    public String getScore(){
        return this.score;
    }
}
