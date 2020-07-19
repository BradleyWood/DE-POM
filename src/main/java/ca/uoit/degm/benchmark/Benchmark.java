package ca.uoit.degm.benchmark;

import ca.uoit.degm.algorithm.Chromosome;

public interface Benchmark {

    double eval(Chromosome chromosome);

    String getName();

}
