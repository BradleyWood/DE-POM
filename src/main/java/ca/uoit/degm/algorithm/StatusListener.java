package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;

import java.util.List;

public interface StatusListener {

    void onStart(List<Chromosome> initialPopulation, Benchmark benchmark);

    void onGeneration(List<Chromosome> population, Benchmark benchmark, int generation);

    void onFinish(List<Chromosome> population, double bestFitness);

}
