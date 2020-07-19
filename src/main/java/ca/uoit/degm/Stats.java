package ca.uoit.degm;

import java.util.Collection;

public class Stats {

    public static double variance(Collection<Double> values, double mean) {
        double total = 0;
        for (Double value : values) {
            total += (value - mean) * (value - mean);
        }
        return total / values.size();
    }

    public static double standardDev(Collection<Double> values, double mean) {
        return Math.sqrt(variance(values, mean));
    }
}
