package test;

import rankingmodel.tf_Idf.TF_IDFCalculator_Parallel;

import java.util.HashMap;
import java.util.Map;

public class TestMax {
    public static void main(String[] args){
        Map<String,Integer> test_map = new HashMap<>();
        test_map.put("one",123);
        test_map.put("two",124);
        test_map.put("zero",0);
        test_map.put("thousand",1000);

        double HIGHEST_FREQUENCY_TERM_COUNT = 0;
        long tStart = System.currentTimeMillis();
        for (Map.Entry<String, Integer> entry : test_map.entrySet()) {
            double value = Double.parseDouble(entry.getValue().toString());
            if (value > HIGHEST_FREQUENCY_TERM_COUNT) {
                HIGHEST_FREQUENCY_TERM_COUNT = value;
            }
        }
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta;
        System.out.println("Elapsed time: "+elapsedSeconds + " miliseconds.");
        System.out.println(HIGHEST_FREQUENCY_TERM_COUNT);
        tStart = System.currentTimeMillis();
        HIGHEST_FREQUENCY_TERM_COUNT = test_map.entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta;
        System.out.println("Elapsed time: "+elapsedSeconds + " miliseconds.");
        System.out.println(HIGHEST_FREQUENCY_TERM_COUNT);
        System.out.println();
    }
}
