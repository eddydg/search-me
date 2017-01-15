package main.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Index {

    private List<Doc> docs;

    public Index() {
        docs = new ArrayList<>();
    }

    public Index(List<Doc> docs) {
        this.docs = docs;
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
}
