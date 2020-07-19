package ca.uoit.degm.benchmark;

import ca.uoit.degm.algorithm.Chromosome;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class JNIBenchmark implements Benchmark {

    private final int num;

    public JNIBenchmark(final int num) {
        this.num = num;
    }

    @Override
    public double eval(final Chromosome chromosome) {
        double[] data = chromosome.getGenes().stream().mapToDouble(a -> a).toArray();

        double result = -1;
        try {
            result = CEC_2014.INSTANCE.cec14_test_func(data, data.length, num);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public String getName() {
        return "F" + num;
    }

    public interface CEC_2014 extends Library {
        CEC_2014 INSTANCE = Native.load("decs.dll", CEC_2014.class);

        double cec14_test_func(double[] data, int nx, int func_num);
    }
}
