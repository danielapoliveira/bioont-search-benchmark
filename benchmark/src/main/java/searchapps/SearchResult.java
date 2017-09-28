package searchapps;

import settings.FieldWeight;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Daniela Oliveira.
 */
public class SearchResult implements Comparable<SearchResult>{

    //Attributes
    String iri;
    String score;
    String label;
    Set<String> synonyms;
    String acronym;
    String definition;
    HashMap<String,String> combined;
    FieldWeight weight;


    public SearchResult(SearchResult s) {
        combined = new HashMap<>();
        this.iri = s.getIri();
        this.score = "";
        this.label = s.getLabel();
        this.synonyms = s.getSynonyms();
        this.acronym = s.getAcronym();
        this.definition = s.getDefinition();
        this.weight = s.getFieldType();
    }

    public SearchResult(){
        combined = new HashMap<>();
        this.iri = "";
        this.score = "";
        this.label = "";
        this.synonyms = new TreeSet<>();
        this.acronym = "";
        this.definition = "";
        this.weight = FieldWeight.LABEL;
    }

    public HashMap<String, String> getCombined() {
        return combined;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
        combined.put("iri",iri);
    }

    public Double getScore() {
        if(!score.equals(""))
            return Double.parseDouble(score);
        else
            return 0.0;
    }

    public void setScore(String score) {
        combined.put("score",score);
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        combined.put("label",label);
        this.label = label;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        combined.put("acronym",acronym);
        this.acronym = acronym;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        combined.put("definition",definition);
        this.definition = definition;
    }

    public String toString() {
        return Arrays.asList(new String[] {
                iri,
                score,
                label,
                synonyms.toString(),
                acronym,
                definition
        }).toString();
    }

    public void setWeight(FieldWeight weight) {
        this.weight = weight;
    }


    public Double getWeight() {
        return weight.getWeight();
    }

    public String getWeightLabel(){
        return weight.getWeightLabel();
    }

    public FieldWeight getFieldType() {return weight.getFieldType();}

    @Override
    public int compareTo(SearchResult c){
        Double scoreDouble = Double.parseDouble(score);
        if(scoreDouble == c.getScore())
            return 0;

        else if(scoreDouble > c.getScore())
            return 1;

        else
            return -1;
    }



    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(iri).
                        append(acronym).
                        toHashCode();
    }



    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SearchResult))
            return false;
        if (obj == this)
            return true;

        SearchResult rhs = (SearchResult) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                        append(iri, rhs.getIri()).
                        append(acronym, rhs.getAcronym()).
                        isEquals();
    }

}
