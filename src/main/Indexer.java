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
public class Indexer {

    private final String STOP_WORDS_FILE = "assets/stops.en.txt";
    private final List<String> suffixesToStrip = new ArrayList<>(Arrays.asList("ed", "ing", "ly"));
    private final int poolSize = 5;

    public HashMap<String, Integer> index;

    public Index run(Stream<Doc> urls) {
        double startTime = System.currentTimeMillis();

        List<Doc> res =  urls
                .map(this::tokenize)
                .map(this::cleanup)
                .map(this::reduce)
                .collect(Collectors.toList());

        double endTime = System.currentTimeMillis();
        Main.logger.info("Index preparation ({}ms)", (endTime - startTime));
        return getIndex(res);
    }

    public Index run(List<String> input) {
        double startTime = System.currentTimeMillis();

        List<Doc> res = input.stream().map(c -> new Doc(null, null, c, null))
                .map(this::tokenize)
                .map(this::cleanup)
                .map(this::reduce)
                .collect(Collectors.toList());

        double endTime = System.currentTimeMillis();
        Main.logger.trace("Index preparation ({}ms)", (endTime - startTime));
        return getIndex(res);
    }

    public Doc cleanup(Doc doc) {
        doc.setTokens(doc.getTokens().stream().map(this::cleanup).collect(Collectors.toList()));
        return doc;
    }

    public Token cleanup(Token token) {
        Document doc = Jsoup.parse(token.getValue());
        token.setValue(doc.text());
        return token;
    }

    public Doc tokenize(Doc doc) {
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

    public Doc reduce(Doc doc) {
        doc.setTokens(doc.getTokens().parallelStream()
                .map(this::reduce)
                .collect(Collectors.toList()));
        return doc;
    }

    public Token reduce(Token token) {
        for (String s: suffixesToStrip) {
            String w = token.getValue();
            if (w.endsWith(s)) {
                token.setValue(w.substring(0, w.length() - s.length()));
                return token;
            }
        }

        return token;
    }

    public double getTermFrequency(Token token, Doc doc) {
        double tokenCount = doc.getTokens().size();
        double result = doc.getTokens().parallelStream()
                .filter(t -> t.getValue().equals(token.getValue()))
                .count();

        return result / tokenCount;
    }

    public double getInverseDocumentFrequency(Token token, List<Doc> docs) {
        double docCount = docs.size();
        double docWithTermCount = docs.parallelStream()
                .filter(doc -> doc.containsWord(token.getValue()))
                .count();

        return Math.log(docCount / docWithTermCount);
    }

    public double getTfidf(Token token, Doc doc, List<Doc> docs) {
        return getTermFrequency(token, doc) * getInverseDocumentFrequency(token, docs);
    }

    public Index getIndex(List<Doc> docs) {
        double startTime = System.currentTimeMillis();
        Index index = new Index(docs);

        ExecutorService es = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < docs.size(); i++) {
            TfidfTask t = new TfidfTask(docs.get(i), docs, i + 1);
            es.execute(t);
        }

        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double endTime = System.currentTimeMillis();
        Main.logger.info("Calculated Index ({}ms)", (endTime - startTime));
        return index;
    }

    private void calculateTfidf(Doc doc, List<Doc> docs) {
        HashMap<String, Double> frequencies = new HashMap<>();

        doc.getTokens().parallelStream()
                .forEach(token -> {
                    double tfidf = getTfidf(token, doc, docs);
                    frequencies.put(token.getValue(), tfidf);
                    token.setFrequence(tfidf);

                });

        doc.setFrequencies(frequencies);
    }

    class TfidfTask extends Thread {
        private final Doc doc;
        private final List<Doc> docs;
        private final int docNumber;

        public TfidfTask(Doc doc, List<Doc> docs, int docNumber) {
            this.doc = doc;
            this.docs = docs;
            this.docNumber = docNumber;
        }

        @Override
        public void run() {
            double startTimeDoc = System.currentTimeMillis();

            calculateTfidf(doc, docs);

            long processedDoc = docs.stream().filter(d -> d.getFrequencies() != null).count();
            double endTimeDoc = System.currentTimeMillis();
            Main.logger.trace("Calculated TFIDF for {} ({}ms) - ({}/{})",
                    doc.getUrl(),
                    (endTimeDoc - startTimeDoc),
                    processedDoc,
                    docs.size());
        }
    }

}
