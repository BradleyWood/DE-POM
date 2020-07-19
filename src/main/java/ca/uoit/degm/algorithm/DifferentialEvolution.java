package ca.uoit.degm.algorithm;

import ca.uoit.degm.benchmark.Benchmark;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static ca.uoit.degm.algorithm.Utility.*;

@Builder
public class DifferentialEvolution implements Algorithm {

    private final List<StatusListener> listeners = new LinkedList<>();
    private final Random random = new Random();

    private @Getter @Setter double crossoverRate;
    private @Getter @Setter double mutationFactor;
    private @Getter @Setter int populationSize;
    private @Getter @Setter double varMin;
    private @Getter @Setter double varMax;
    private @Getter @Setter int nfcFactor;
    private @Getter @Setter int dim;

    @Override
    public Chromosome run(final Benchmark benchmark) {
        final List<Chromosome> population = Utility.createPopulation(populationSize, dim, varMin, varMax);

        final int nfc = nfcFactor * dim;

        listeners.forEach(l -> l.onStart(population, benchmark));

        for (int g = 0; g < nfc / populationSize; g++) {
            for (int i = 0; i < populationSize; i++) {
                int Xa = selectParent(i);
                int Xb = selectParent(i, Xa);
                int Xc = selectParent(i, Xa, Xb);

                final Chromosome Xi = population.get(i);
                final List<Double> Vi =
                        add(
                                population.get(Xa).getGenes(),
                                scalarMult(
                                        sub(
                                                population.get(Xc).getGenes(),
                                                population.get(Xb).getGenes()
                                        ),
                                        mutationFactor
                                )
                        );

                final Chromosome Ui = new Chromosome(dim);

                for (int j = 0; j < dim; j++) {
                    if (random.nextFloat() < crossoverRate) {
                        Ui.add(Vi.get(j));
                    } else {
                        Ui.add(Xi.get(j));
                    }
                }

                double bUi = benchmark.eval(Ui);

                Ui.setFitness(bUi);

                if (Xi.getFitness() == null) {
                    double bXi = benchmark.eval(Xi);
                    Xi.setFitness(bXi);
                }

                if (bUi < Xi.getFitness()) {
                    population.set(i, Ui);
                }
            }

            final int generation = g;
            listeners.forEach(l -> l.onGeneration(population, benchmark, generation));
        }

        final Optional<Chromosome> min = population.stream().min(Comparator.comparingDouble((chromosome) -> {
            if (chromosome.getFitness() == null) {
                chromosome.setFitness(benchmark.eval(chromosome));
            }
            return chromosome.getFitness();
        }));

        min.ifPresent(chromosome -> listeners.forEach(l -> l.onFinish(population, chromosome.getFitness())));

        return Collections.min(population, Comparator.comparingDouble((chromosome) -> {
            return chromosome.getFitness() != null ? chromosome.getFitness() : benchmark.eval(chromosome);
        }));
    }

    @Override
    public String getName() {
        return "DE";
    }

    private int selectParent(int... indices) {
        int r;

        do {
            r = random.nextInt(populationSize);
        } while (contains(r, indices));

        return r;
    }

    private boolean contains(int target, int... values) {
        for (int value : values) {
            if (value == target)
                return true;
        }
        return false;
    }

    public void printProperties() {

    }

    @Override
    public void addStatusListener(final StatusListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeStatusListener(final StatusListener listener) {
        listeners.remove(listener);
    }
}
