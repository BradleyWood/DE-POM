package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;

public interface Algorithm {

    void addStatusListener(StatusListener listener);

    void removeStatusListener(StatusListener listener);

    Chromosome run(Benchmark benchmark);

    void setDim(int dim);

    int getDim();

    String getName();

}
