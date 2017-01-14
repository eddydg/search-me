package main.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Index {

    private List<Doc> docs;
    private HashMap<String, Double> frequencies;

    public Index() {
        docs = new ArrayList<>();
        frequencies = new HashMap<>();
    }

    public Index(List<Doc> docs, HashMap<String, Double> frequencies) {
        this.docs = docs;
        this.frequencies = frequencies;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public boolean containsWord(String word) {
        return docs.stream().anyMatch(doc -> doc.containsWord(word));
    }

    public HashMap<String, Double> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(HashMap<String, Double> frequencies) {
        this.frequencies = frequencies;
    }
}
