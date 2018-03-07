package algorithmexecuter.model;

import algorithmexecuter.enums.FixedAlgorithmNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AlgorithmStorage {

    private final List<Algorithm> algorithms = new ArrayList<>();

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public void clearAlgorithmStorage() {
        this.algorithms.clear();
    }

    public void add(Algorithm alg) {
        this.algorithms.add(alg);
    }

    public void addAll(Collection<Algorithm> algorithms) {
        algorithms.forEach((alg) -> {
            this.algorithms.add(alg);
        });
    }
    
    public void remove(Algorithm alg) {
        this.algorithms.remove(alg);
    }

    public AlgorithmStorage() {
    }

    public AlgorithmStorage(List<Algorithm> algorithms) {
        this.algorithms.clear();
        this.algorithms.addAll(algorithms);
    }

    public Algorithm getMainAlgorithm() {
        return getAlgorithmByName(FixedAlgorithmNames.MAIN.getValue());
    }

    public Algorithm getAlgorithmByName(String name) {
        for (Algorithm alg : this.algorithms) {
            if (alg.getName().equals(name)) {
                return alg;
            }
        }
        return null;
    }

}
