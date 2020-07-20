package ca.uoit.degm.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Utility {

    private static final Random random = new Random();

    public static List<Chromosome> oppositePopulation(final List<Chromosome> population, final int dim) {
        final List<Chromosome> opposites = new ArrayList<>();
        double[] minimums = getMinimums(population, dim);
        double[] maximums = getMaximums(population, dim);

        for (final Chromosome chromosome : population) {
            final Chromosome copy = new Chromosome(dim);

            for (int i = 0; i < dim; i++) {
                copy.add(minimums[i] + maximums[i] - chromosome.get(i));
            }

            opposites.add(copy);
        }

        return opposites;
    }

    public static List<Chromosome> oppositePopulationCenterBased(final List<Chromosome> population, final int dim) {
        final List<Chromosome> opposites = new ArrayList<>();
        double[] averages = getAverages(population, dim);

        for (final Chromosome chromosome : population) {
            final Chromosome copy = new Chromosome(dim);

            for (int i = 0; i < dim; i++) {
                copy.add((2 * averages[i]) - chromosome.get(i));
            }

            opposites.add(copy);
        }

        return opposites;
    }

    public static List<Double> getNthGenes(final List<Chromosome> chromosomes, final int idx) {
        final List<Double> genes = new ArrayList<>(chromosomes.size());

        for (final Chromosome chromosome : chromosomes) {
            genes.add(chromosome.get(idx));
        }

        return genes;
    }

    public static int getLowestEuclideanDistance(final List<Chromosome> chromosomes, final int d, final int gene) {
        final List<Double> nthGenes = getNthGenes(chromosomes, gene);
        final double[] distances = new double[chromosomes.size()];
        double min = Double.MAX_VALUE;
        int minIdx = 0;

        for (int i = 0; i < chromosomes.size(); i++) {
            distances[i] = getEuclideanDistance(chromosomes.get(i).get(gene), nthGenes);
        }

        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < min) {
                min = distances[i];
                minIdx = i;
            }
        }

        return minIdx;
    }

    public static double getEuclideanDistance(final double gene, final List<Double> correspondingGenes) {
        double total = 0;

        for (int i = 0; i < correspondingGenes.size(); i++) {
            total += Math.pow(gene - correspondingGenes.get(i), 2);
        }

        return Math.sqrt(total);
    }

    public static List<Double> oppositeCopy(final List<Double> v, final double min, final double max) {
        final List<Double> ret = new ArrayList<>(v.size());

        for (final Double gene : v) {
            ret.add(min + max - gene);
        }

        return ret;
    }

    public static List<Double> scalarMult(final List<Double> v, final double factor) {
        final List<Double> ret = new ArrayList<>(v.size());

        for (int i = 0; i < v.size(); i++) {
            ret.add(v.get(i) * factor);
        }

        return ret;
    }

    public static double[] getAverages(List<Chromosome> chromosomes, int dim) {
        double[] averages = new double[dim];

        for (int d = 0; d < dim; d++) {
            averages[d] = 0;

            for (Chromosome chromosome : chromosomes) {
                averages[d] += chromosome.get(d);
            }

            averages[d] /= chromosomes.size();
        }

        return averages;
    }

    public static double[] getMinimums(List<Chromosome> chromosomes, int dim) {
        double[] minimums = new double[dim];

        for (int d = 0; d < dim; d++) {
            minimums[d] = Double.POSITIVE_INFINITY;
            for (Chromosome chromosome : chromosomes) {
                if (chromosome.get(d) < minimums[d]) {
                    minimums[d] = chromosome.get(d);
                }
            }
        }

        return minimums;
    }

    public static double[] getMaximums(List<Chromosome> chromosomes, int dim) {
        double[] maximums = new double[dim];

        for (int d = 0; d < dim; d++) {
            maximums[d] = Double.NEGATIVE_INFINITY;
            for (Chromosome chromosome : chromosomes) {
                if (chromosome.get(d) > maximums[d]) {
                    maximums[d] = chromosome.get(d);
                }
            }
        }

        return maximums;
    }

    public static List<Double> add(final List<Double> v1, final List<Double> v2) {
        if (v1.size() != v2.size())
            throw new IllegalArgumentException("Vector sizes are not equal");

        final List<Double> ret = new Vector<>(v1.size());

        for (int i = 0; i < v1.size(); i++) {
            ret.add(v1.get(i) + v2.get(i));
        }
        return ret;
    }

    public static List<Double> sub(final List<Double> v1, final List<Double> v2) {
        if (v1.size() != v2.size())
            throw new IllegalArgumentException("List sizes are not equal");

        final List<Double> ret = new ArrayList<>(v1.size());

        for (int i = 0; i < v1.size(); i++) {
            ret.add(v1.get(i) - v2.get(i));
        }
        return ret;
    }

    public static List<Chromosome> createPopulation(final int size, final int dim, final double min, final double max) {
        final ArrayList<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            population.add(new Chromosome(createIndividual(dim, min, max)));
        }

        return population;
    }

    public static List<Double> createIndividual(final int dim, final double min, final double max) {
        final List<Double> v = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            v.add(min + (max - min) * random.nextDouble());
        }
        return v;
    }

}
