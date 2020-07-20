package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static ca.uoit.degm.algorithm.Utility.*;

public class POB implements Algorithm, StatusListener {

    private final Random random = new Random();
    private final Algorithm algorithm;

    private @Getter @Setter double jumpRate;

    public POB(final Algorithm algorithm, final double jumpRate) {
        this.algorithm = algorithm;
        this.jumpRate = jumpRate;
        algorithm.addStatusListener(this);
    }

    @Override
    public void addStatusListener(final StatusListener listener) {
        algorithm.addStatusListener(listener);
    }

    @Override
    public void removeStatusListener(final StatusListener listener) {
        algorithm.removeStatusListener(listener);
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
    public String getName() {
        return "DE-POB";
    }

    @Override
    public void onStart(final List<Chromosome> initialPopulation, final Benchmark benchmark) {
        int populationSize = initialPopulation.size();

        final List<Chromosome> opposites = oppositePopulation(initialPopulation, getDim());
        initialPopulation.addAll(opposites);

        initialPopulation.forEach((chromosome) -> {
            if (chromosome.getFitness() == null) {
                chromosome.setFitness(benchmark.eval(chromosome));
            }
        });

        initialPopulation.sort(Comparator.comparingDouble(Chromosome::getFitness));

        for (int i = initialPopulation.size() - 1; i > populationSize - 1; i--) {
            initialPopulation.remove(i);
        }
    }

    @Override
    public int getDim() {
        return algorithm.getDim();
    }

    @Override
    public void onGeneration(List<Chromosome> population, Benchmark benchmark, int generation) {
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

    private List<Chromosome> calculatePartialOpposites(final List<Chromosome> population, final Benchmark benchmark) {
        final List<Chromosome> par_opp = new ArrayList<>();
        int dim = population.get(0).size();

        final Chromosome ref = getBest(population, benchmark);
        double[] minimums = getMinimums(population, dim);
        double[] maximums = getMaximums(population, dim);

        for (int k = 0; k < population.size(); k++) {
            final List<Double> newTrial = new ArrayList<>();
            int iter_var = 0;
            int iter_var_opp = 0;

            for (int j = 0; j < dim; j++) {
                double ref_j = ref.get(j);
                double p_kj = population.get(k).get(j);
                double p_kj_opp = minimums[j] + maximums[j] - p_kj;

                if (Math.abs(ref_j - p_kj) < Math.abs(ref_j - p_kj_opp)) {
                    newTrial.add(p_kj);
                    iter_var++;
                } else {
                    newTrial.add(p_kj_opp);
                    iter_var_opp++;
                }
            }

            if (iter_var < iter_var_opp) {
                par_opp.add(new Chromosome(newTrial));
            }
        }

        return par_opp;
    }

    private Chromosome getBest(final List<Chromosome> population, final Benchmark benchmark) {
        return population.stream().min(Comparator.comparingDouble((chromosome) -> {
            if (chromosome.getFitness() == null) {
                chromosome.setFitness(benchmark.eval(chromosome));
            }
            return chromosome.getFitness();
        })).orElseThrow(RuntimeException::new);
    }

    @Override
    public void onFinish(List<Chromosome> population, double bestFitness) {

    }
}
