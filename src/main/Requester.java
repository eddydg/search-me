package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Result;
import main.Models.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by MeltedPenguin on 16/01/2017.
 */
public class Requester {

    private int maxResult = 10;

    Index index;

    public Requester(Index index) {
        this.index = index;
    }

    public Requester(Index index, int maxResult) {
        this.index = index;
        this.maxResult = maxResult;
    }

    public List<Result> search(String keywords) {
        Doc searchDoc = new Doc();
        searchDoc.setContent(keywords);
        Indexer indexer = new Indexer();
        searchDoc = indexer.tokenize(searchDoc);
        searchDoc = indexer.reduce(searchDoc);

        return find(searchDoc);
    }

    private List<Result> find(Doc searchDoc) {
        List<Result> results = new ArrayList<>();
        for (Doc doc: index.getDocs()) {
            Result result = new Result(doc);
            double score = 0;

            for (Token token: searchDoc.getTokens()) {
                if (doc.getFrequencies().containsKey(token.getValue())) {
                    score += doc.getFrequencies().get(token.getValue());
                }
            }

            result.setScore(score);
            results.add(result);
        }

        results.sort(Comparator.comparingDouble(Result::getScore).reversed());

        return results.subList(0, Math.min(results.size(), maxResult));
    }
}
