package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static ca.uoit.degm.algorithm.Utility.*;

public class POM implements Algorithm, StatusListener {

    private final Random random = new Random();
    private final Algorithm algorithm;

    private @Getter @Setter double jumpRate;
    private @Getter @Setter double referenceFactor;

    public POM(final Algorithm algorithm, final double jumpRate, final double referenceFactor) {
        this.algorithm = algorithm;
        this.jumpRate = jumpRate;
        this.referenceFactor = referenceFactor;
        algorithm.addStatusListener(this);
    }

    @Override
    public Chromosome run(Benchmark benchmark) {
        return algorithm.run(benchmark);
    }

    @Override
    public void setDim(int dim) {
        algorithm.setDim(dim);
    }

    @Override
    public void onStart(final List<Chromosome> initialPopulation, final Benchmark benchmark) {

    }

    @Override
    public String getName() {
        return "DE-POM";
    }

    private List<Chromosome> calculatePartialOpposites(final List<Chromosome> population, final Benchmark benchmark) {
        final List<Chromosome> par_opp = new ArrayList<>();
        int dim = population.get(0).size();

        population.sort(Comparator.comparingDouble((chromosome) -> {
            if (chromosome.getFitness() == null) {
                chromosome.setFitness(benchmark.eval(chromosome));
            }
            return chromosome.getFitness();
        }));

        final List<Chromosome> topChromosomes = population.stream()
                .limit(Math.round(referenceFactor * population.size()))
                .collect(Collectors.toList());

        final int[] bestIndices = new int[dim];

        for (int i = 0; i < dim; i++) {
            bestIndices[i] = getClosestToMean(topChromosomes, i);
        }

        Chromosome ref = new Chromosome(dim);

        for (int i = 0; i < bestIndices.length; i++) {
            ref.add(topChromosomes.get(bestIndices[i]).get(i));
        }

        double refFitness = benchmark.eval(ref);

        if (refFitness > population.get(0).getFitness()) {
            ref = population.get(0);
        }

        double[] minimums = getMinimums(population, dim);
        double[] maximums = getMaximums(population, dim);

        for (int i = 0; i < population.size(); i++) {
            final Chromosome newTrial = new Chromosome(dim);
            int iter_var = 0;
            int iter_var_opp = 0;

            for (int j = 0; j < dim; j++) {
                double ref_j = ref.get(j);
                double g_ij = population.get(i).get(j);
                double g_ij_opp = minimums[j] + maximums[j] - g_ij;

                if (Math.abs(ref_j - g_ij) < Math.abs(ref_j - g_ij_opp)) {
                    newTrial.add(g_ij);
                    iter_var++;
                } else {
                    newTrial.add(g_ij_opp);
                    iter_var_opp++;
                }
            }

            if (iter_var < iter_var_opp) {
                par_opp.add(newTrial);
            }
        }

        par_opp.add(ref);

        return par_opp;
    }

    @Override
    public int getDim() {
        return algorithm.getDim();
    }

    @Override
    public void onGeneration(final List<Chromosome> population, final Benchmark benchmark, final int generation) {
        if (random.nextFloat() < jumpRate && !population.isEmpty()) {
            int populationSize = population.size();

            population.addAll(calculatePartialOpposites(population, benchmark));

            population.sort(Comparator.comparingDouble((chromosome) -> {
                if (chromosome.getFitness() == null) {
                    chromosome.setFitness(benchmark.eval(chromosome));
                }
                return chromosome.getFitness();
            }));

            for (int i = population.size() - 1; i > populationSize - 1; i--) {
                population.remove(i);
            }
        }
    }

    @Override
    public void onFinish(List<Chromosome> population, double bestFitness) {

    }

    @Override
    public void addStatusListener(StatusListener listener) {
        algorithm.addStatusListener(listener);
    }

    @Override
    public void removeStatusListener(StatusListener listener) {
        algorithm.removeStatusListener(listener);
    }
}
