package ca.uoit.degm.algorithm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data class Chromosome {

    private final List<Double> genes;
    private Double fitness = null;

    public Chromosome(final List<Double> genes) {
        this.genes = genes;
    }

    public Chromosome(int dim) {
        this(new ArrayList<>(dim));
    }

    public void set(int idx, double value) {
        genes.set(idx, value);
        fitness = null;
    }

    public double get(int idx) {
        return genes.get(idx);
    }

    public void add(double value) {
        genes.add(value);
        fitness = null;
    }

    public int size() {
        return genes.size();
    }
}
