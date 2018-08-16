package rbfn;

import java.util.List;

public class Data {
    private final List<Pattern> patterns;
    private final List<Character> labels;

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public List<Character> getLabels() {
        return labels;
    }

    public Data(List<Pattern> patterns, List<Character> labels){
        this.patterns = patterns;
        this.labels = labels;
    }
}
