package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static ca.uoit.degm.algorithm.Utility.oppositePopulation;

public class ODE implements Algorithm, StatusListener {

    private final Random random = new Random();
    private final Algorithm algorithm;

    private @Getter @Setter double jumpRate;

    public ODE(final Algorithm algorithm, final double jumpRate) {
        this.algorithm = algorithm;
        this.jumpRate = jumpRate;
        algorithm.addStatusListener(this);
    }

    @Override
    public void onStart(final List<Chromosome> initialPopulation, final Benchmark benchmark) {

    }

    @Override
    public void setDim(int dim) {
        algorithm.setDim(dim);
    }

    @Override
    public String getName() {
        return "ODE";
    }

    @Override
    public void onGeneration(final List<Chromosome> population, final Benchmark benchmark, final int generation) {
        if (random.nextFloat() < jumpRate && !population.isEmpty()) {
            int populationSize = population.size();
            final List<Chromosome> opposites = oppositePopulation(population, population.get(0).size());
//            final List<Chromosome> opposites = oppositePopulationCenterBased(population, population.get(0).size());
            population.addAll(opposites);

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
    public void onFinish(final List<Chromosome> population, final double bestFitness) {

    }

    @Override
    public void addStatusListener(final StatusListener listener) {
        algorithm.addStatusListener(listener);
    }

    @Override
    public void removeStatusListener(final StatusListener listener) {
        algorithm.addStatusListener(listener);
    }

    @Override
    public Chromosome run(final Benchmark benchmark) {
        return algorithm.run(benchmark);
    }
}
