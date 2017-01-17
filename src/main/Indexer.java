package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Token;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Indexer implements Runnable {

    private static final String STOP_WORDS_FILE = "assets/stops.en.txt";
    private static final List<String> suffixesToStrip = new ArrayList<>(Arrays.asList("ed", "ing", "ly"));
    private final Index index;
    private final BlockingQueue<Doc> crawledDocs;

    public Indexer(Index index, BlockingQueue<Doc> crawledDocs) {
        this.index = index;
        this.crawledDocs = crawledDocs;
    }

    @Override
    public void run() {
        Doc currentDoc;
        while (true) {
            try {
                currentDoc = crawledDocs.take();
                currentDoc = reduce(cleanup(tokenize(currentDoc)));
                index.getDocs().add(currentDoc);
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        }

    }
    static public Doc cleanup(Doc doc) {
        doc.setTokens(doc.getTokens().stream().map(Indexer::cleanup).collect(Collectors.toList()));
        return doc;
    }

    static public Token cleanup(Token token) {
        Document doc = Jsoup.parse(token.getValue());
        token.setValue(doc.text());
        return token;
    }

    static public Doc tokenize(Doc doc) {
        String[] words = doc.getContent().toLowerCase().split("\\W+");

        File file = new File(STOP_WORDS_FILE);
        try {
            List<String> stopWords = Files.readAllLines(file.toPath(), Charset.defaultCharset());

            long count = 0;
            List<Token> tokens = new ArrayList<>();

            for (String word: words) {
                if (!stopWords.contains(word)) {
                    tokens.add(new Token(word, 0, count));
                    count++;
                }
            }

            doc.setTokens(tokens);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    static public Doc reduce(Doc doc) {
        doc.setTokens(doc.getTokens().parallelStream()
                .map(Indexer::reduce)
                .collect(Collectors.toList()));
        return doc;
    }

    static public Token reduce(Token token) {
        for (String s: suffixesToStrip) {
            String w = token.getValue();
            if (w.endsWith(s)) {
                token.setValue(w.substring(0, w.length() - s.length()));
                return token;
            }
        }

        return token;
    }

    static public double getTermFrequency(Token token, Doc doc) {
        double tokenCount = doc.getTokens().size();
        double result = doc.getTokens().parallelStream()
                .filter(t -> t.getValue().equals(token.getValue()))
                .count();

        return result / tokenCount;
    }

    static public double getInverseDocumentFrequency(Token token, List<Doc> docs) {
        double docCount = docs.size();
        double docWithTermCount = 0;

        for (Doc doc: docs) {
            if (doc.getTokens().contains(token)) {
                docWithTermCount++;
                break;
            }
        }

        return Math.log(docCount / docWithTermCount);
    }

    static public double getTfidf(Token token, Doc doc, List<Doc> docs) {
        return getTermFrequency(token, doc) * getInverseDocumentFrequency(token, docs);
    }


}
