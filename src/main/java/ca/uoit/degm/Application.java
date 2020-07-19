package ca.uoit.degm;

import ca.uoit.degm.algorithm.*;
import ca.uoit.degm.benchmark.Benchmark;
import ca.uoit.degm.benchmark.JNIBenchmark;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Application {

    public static final int POPULATION_SIZE = 100;
    public static final int VAR_MIN = -100;
    public static final int VAR_MAX = 100;
    public static final double CROSSOVER_RATE = 0.9;
    public static final double MUTATION_FACTOR = 0.5;
    public static final int NFC_FACTOR = 1000;
    public static final int NUM_RUNS = 50;
    public static final double JUMP_RATE = 0.3;
    public static final double REFERENCE_FACTOR = 0.1;
    public static final int[] DIMENSIONS = {30, 50, 100};

    public static void main(String[] args) {
        final List<Benchmark> benchmarks = new LinkedList<>();

        for (int i = 1; i <= 30; i++) {
            benchmarks.add(new JNIBenchmark(i));
        }

        final List<Algorithm> algorithms = Arrays.asList(
//                getDE()
//                new ODE(getDE(), JUMP_RATE)
                new POM(getDE(), JUMP_RATE, REFERENCE_FACTOR)
        );

        System.out.println("Algorithms=" + algorithms);
        System.out.println("Mutation Factor=" + MUTATION_FACTOR);
        System.out.println("Dims=" + Arrays.toString(DIMENSIONS));
        System.out.println("Num Runs=" + NUM_RUNS);

        createTables(algorithms, benchmarks, DIMENSIONS);
    }

    public static Algorithm getDE() {
        return DifferentialEvolution.builder()
                .populationSize(POPULATION_SIZE)
                .crossoverRate(CROSSOVER_RATE)
                .mutationFactor(MUTATION_FACTOR)
                .varMin(VAR_MIN)
                .varMax(VAR_MAX)
                .nfcFactor(NFC_FACTOR)
                .dim(100).build();
    }

    private static void createTables(final List<Algorithm> algorithms, final List<Benchmark> benchmarks, final int[] dimensions) {
        for (Algorithm algorithm : algorithms) {
            final File wbFile = new File("algorithm-" + algorithm.getName() + "F" + MUTATION_FACTOR + ".xls");
            final Workbook wb = new HSSFWorkbook();

            Sheet[] sheets = new HSSFSheet[dimensions.length];
            for (int i = 0; i < sheets.length; i++) {
                sheets[i] = wb.createSheet("Table dim=" + dimensions[i]);
            }

            Arrays.stream(sheets).forEach(sh -> {
                sh.createRow(0).createCell(0).setCellValue("FitnessFunction");
                sh.getRow(0).createCell(1).setCellValue("Mean");
                sh.getRow(0).createCell(2).setCellValue("Best");
                sh.getRow(0).createCell(3).setCellValue("Worst");
                sh.getRow(0).createCell(4).setCellValue("Standard Dev");
                int i = 1;
                for (Benchmark benchmark : benchmarks) {
                    sh.createRow(i++).createCell(0).setCellValue(benchmark.getName());
                }
            });

            int bmCountRow = 1;
            for (Benchmark bm : benchmarks) {
                for (int i = 0; i < dimensions.length; i++) {
                    algorithm.setDim(dimensions[i]);
                    List<Double> results = runTest(algorithm, bm, NUM_RUNS);
                    for (int j = 1; j <= results.size(); j++) {
                        sheets[i].getRow(bmCountRow).createCell(j).setCellValue(results.get(j - 1));
                    }
                }
                bmCountRow++;
            }

            try {
                FileOutputStream fos = new FileOutputStream(wbFile);
                wb.write(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void plotRuns(final List<Algorithm> algorithms, final List<Benchmark> benchmarks,
                                final int[] dimensions, final int numRuns, final String folder) {
        final PlotListener plotListener = new PlotListener();
        algorithms.forEach(a -> a.addStatusListener(plotListener));

        for (final Benchmark benchmark : benchmarks) {
            for (final int d : dimensions) {
                for (final Algorithm algorithm : algorithms) {
                    final List<List<Double>> fitnessRuns = new LinkedList<>();

                    for (int i = 0; i < numRuns; i++) {
                        algorithm.setDim(d);
                        algorithm.run(benchmark);
                        fitnessRuns.add(plotListener.getBestFitnessValues());
                    }

                    plotFitnessVsNfc(fitnessRuns, folder + "/" + algorithm.getName() + "-" + benchmark.getName()
                            + "-D=" + d + ".xls");
                    System.out.println("Plot " + algorithm.getName() + " d=" + d + " " + benchmark.getName());
                }
            }
        }
    }

    @SneakyThrows
    private static void plotFitnessVsNfc(final List<List<Double>> fitnessRuns, final String file) {
        final Workbook wb = new HSSFWorkbook();
        final Sheet sheet = wb.createSheet("Best Fitness Vs NFC");
        int row_count = 0;
        double generation = 0;

        sheet.createRow(row_count).createCell(0).setCellValue("NFC");
        sheet.getRow(row_count++).createCell(1).setCellValue("Best Fitness");

        final double[] totals = new double[fitnessRuns.get(0).size()];

        for (final List<Double> fitnessRun : fitnessRuns) {
            for (int i = 0; i < fitnessRun.size(); i++) {
                totals[i] += fitnessRun.get(i);
            }
        }

        for (double total : totals) {
            Row row = sheet.createRow(row_count++);
            Cell nfc_cell = row.createCell(0);
            Cell bestFitness = row.createCell(1);
            nfc_cell.setCellValue(generation);
            bestFitness.setCellValue(total / fitnessRuns.size());
            generation++;
        }

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }

    public static List<Double> runTest(final Algorithm alg, final Benchmark fitnessFunction, final int numRuns) {
        Collection<Chromosome> list = Collections.synchronizedCollection(new ArrayList<>());
        long start = System.currentTimeMillis();

        for (int i = 0; i < numRuns; i++) {
            list.add(alg.run(fitnessFunction));
            System.out.println("Run complete: " + i);
        }

        LinkedList<Double> fitnessValues = new LinkedList<>();
        long finish = System.currentTimeMillis() - start;
        double worstFitness = Double.NEGATIVE_INFINITY;
        double bestFitness = Double.POSITIVE_INFINITY;
        double total = 0;
        int dim = 0;

        for (Chromosome v : list) {
            double fitness = fitnessFunction.eval(v);
            fitnessValues.add(fitness);
            dim = v.getGenes().size();
            total += fitness;
            if (fitness > worstFitness) {
                worstFitness = fitness;
            }
            if (fitness < bestFitness) {
                bestFitness = fitness;
            }
        }

        double mean = total / numRuns;
        double stdev = Stats.standardDev(fitnessValues, mean);

        List<Double> results = new ArrayList<>();
        results.add(mean);
        results.add(bestFitness);
        results.add(worstFitness);
        results.add(stdev);

        System.out.println(
                "Mean: "
                        + String.format("%6.4e", mean)
                        + " best: "
                        + String.format("%6.4e", bestFitness)
                        + " worst: "
                        + String.format("%6.4e", worstFitness)
                        + " stdev: "
                        + String.format("%6.4e", stdev)
                        + " DIM="
                        + dim
                        + " "
                        + fitnessFunction.getName()
                        + " Time= " + finish);
        return results;
    }
}
