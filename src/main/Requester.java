package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Result;
import main.Models.Token;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by MeltedPenguin on 16/01/2017.
 */
public class Requester {

    private int maxResult = 10;

    private Index index;

    public Requester(Index index) {
        this.index = index;
    }

    public Requester(Index index, int maxResult) {
        this.index = index;
        this.maxResult = maxResult;
    }

    public void prompt() {
        Scanner in = new Scanner(System.in);
        String query;

        System.out.print("Search for ('\\q' to quit): ");
        while (!(query = in.nextLine()).equals("\\q")) {
            List<Result> results = search(query);
            results.forEach(System.out::println);
            System.out.print("Search for ('\\q' to quit): ");
        }
    }

    public List<Result> search(String keywords) {
        long startTime = System.currentTimeMillis();
        Doc searchDoc = new Doc();
        searchDoc.setContent(keywords);
        Indexer.tokenize(searchDoc);
        Indexer.reduce(searchDoc);

        List<Result> results = find(searchDoc);

        long endTime = System.currentTimeMillis();
        Main.logger.trace("End of searching for {} ({}ms)", searchDoc.getTokens(), (endTime - startTime));

        return results;
    }

    private List<Result> find(Doc searchDoc) {
        List<Result> results = new ArrayList<>();
        List<Doc> docs = index.getDocs();

        docs.forEach(doc -> results.add(new Result(doc, searchDoc.getTokens().stream()
                .map(t -> Indexer.getTfidf(t, doc, docs))
                .mapToInt(Double::intValue)
                .sum()))
        );

        results.sort(Comparator.comparingDouble(Result::getScore).reversed());

        return results
                .subList(0, Math.min(results.size(), maxResult)).stream()
                .filter(r -> r.getScore() > 0)
                .collect(Collectors.toList());
    }
}
