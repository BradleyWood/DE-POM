package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public @Data class PlotListener implements StatusListener {

    private List<Double> bestFitnessValues = null;

    @Override
    public void onStart(final List<Chromosome> initialPopulation, final Benchmark benchmark) {
        bestFitnessValues = new ArrayList<>();
        bestFitnessValues.add(getBest(initialPopulation, benchmark).getFitness());
    }

    @Override
    public void onGeneration(final List<Chromosome> population, final Benchmark benchmark, final int generation) {
        bestFitnessValues.add(getBest(population, benchmark).getFitness());
    }

    @Override
    public void onFinish(final List<Chromosome> population, final double bestFitness) {

    }

    private Chromosome getBest(final List<Chromosome> population, final Benchmark benchmark) {
        return population.stream().min(Comparator.comparingDouble((chromosome) -> {
            if (chromosome.getFitness() == null) {
                chromosome.setFitness(benchmark.eval(chromosome));
            }
            return chromosome.getFitness();
        })).orElseThrow(RuntimeException::new);
    }
}
