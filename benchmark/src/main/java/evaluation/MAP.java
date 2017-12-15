package evaluation;

import java.util.List;

import static java.lang.Double.min;

/**
 * Created by Daniela Oliveira
 */
public class MAP {
    private MAP(){}

    public static double apk( List<String> ranked_items, List<String> correct_items, int k){
        if(ranked_items.size() > k){
            ranked_items = ranked_items.subList(0,k);
        }

        double score = 0.0;
        double num_hits = 0.0;
        for(int i=0 ; i < ranked_items.size() ; i++){
            if(correct_items.contains(ranked_items.get(i))){
                num_hits++;
                score += num_hits / (i+1.0);
            }
        }

        if(correct_items.isEmpty())
            return 0.0;
        return score / min(correct_items.size(),k);
    }

}
