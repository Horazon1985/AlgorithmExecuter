package algorithmexecuter.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlgorithmSignatureStorage {

    private final List<Signature> algorithmSignatures = new ArrayList<>();

    public List<Signature> getAlgorithmSignatures() {
        return algorithmSignatures;
    }

    public void clearAlgorithmSignatureStorage() {
        this.algorithmSignatures.clear();
    }

    public void add(Signature sgn) {
        this.algorithmSignatures.add(sgn);
        Collections.sort(this.algorithmSignatures);
    }

    public void remove (Signature sgn) {
        this.algorithmSignatures.remove(sgn);
    }
    
}
