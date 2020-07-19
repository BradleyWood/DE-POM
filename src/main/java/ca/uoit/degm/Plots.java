package ca.uoit.degm;

import ca.uoit.degm.algorithm.Algorithm;
import ca.uoit.degm.algorithm.ODE;
import ca.uoit.degm.algorithm.POM;
import ca.uoit.degm.benchmark.Benchmark;
import ca.uoit.degm.benchmark.JNIBenchmark;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Plots {

    public static void main(String[] args) {
        final List<Benchmark> benchmarks = new LinkedList<>();

        benchmarks.add(new JNIBenchmark(5));
        benchmarks.add(new JNIBenchmark(8));
        benchmarks.add(new JNIBenchmark(12));
        benchmarks.add(new JNIBenchmark(17));
        benchmarks.add(new JNIBenchmark(24));
        benchmarks.add(new JNIBenchmark(28));

        final List<Algorithm> algorithms = Arrays.asList(
                Application.getDE(),
                new ODE(Application.getDE(), Application.JUMP_RATE),
                new POM(Application.getDE(), Application.JUMP_RATE, Application.REFERENCE_FACTOR)
        );

        Application.plotRuns(algorithms, benchmarks, new int[]{100}, 50, "plot");
    }
}
