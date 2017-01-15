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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Indexer {

    private static String STOP_WORDS_FILE = "assets/stops.en.txt";
    private static List<String> suffixesToStrip = new ArrayList<>(Arrays.asList("ed", "ing", "ly"));

    public HashMap<String, Integer> index;

    public static Index run(Stream<URL> urls) {
        List<Doc> res =  urls
                .map(Indexer::fetchDocument)
                .map(Indexer::tokenize)
                .map(Indexer::cleanup)
                .map(Indexer::reduce)
                .collect(Collectors.toList());

        return tfidf(res);
    }

    public static Doc fetchDocument(URL url) {
        String content = "";
        try {
            Document doc = Jsoup.connect(url.toString()).get();
            content = doc.text();
        } catch (UnsupportedMimeTypeException e) {
            Main.logger.warn("Unsupported Mime Type: " + url);
        } catch (IOException e) {
            Main.logger.warn("HTTP error fetching URL: " + url);
        }
        return new Doc(url, null, content, null);
    }

    public static Doc cleanup(Doc doc) {
        doc.setTokens(doc.getTokens().stream().map(Indexer::cleanup).collect(Collectors.toList()));
        return doc;
    }

    public static Token cleanup(Token token) {
        Document doc = Jsoup.parse(token.getValue());
        token.setValue(doc.text());
        return token;
    }

    public static Doc tokenize(Doc doc) {
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

    public static Doc reduce(Doc doc) {
        doc.setTokens(doc.getTokens().stream().map(Indexer::reduce).collect(Collectors.toList()));
        return doc;
    }

    public static Token reduce(Token token) {
        for (String s: suffixesToStrip) {
            String w = token.getValue();
            if (w.endsWith(s)) {
                token.setValue(w.substring(0, w.length() - s.length()));
                return token;
            }
        }

        return token;
    }

    public static double getTermFrequency(Token token, Doc doc) {
        double result = doc.getTokens().stream().filter(t -> t.getValue().equalsIgnoreCase(token.getValue())).count();
        double size = doc.getTokens().size();
        return result / size;
    }

    public static double getInverseTermFrequency(Token token, List<Doc> docs) {
        //double d = docs.size();
        //double count = docs.parallelStream().filter(doc -> doc.containsWord(token.getValue())).count();

        double n = 0;
        for (Doc doc : docs) {
            for (Token word : doc.getTokens()) {
                if (word.getValue().equalsIgnoreCase(token.getValue())) {
                    n++;
                    break;
                }
            }
        }

        //return Math.log(d / count);
        return Math.log(docs.size() / n);
    }

    public static Index tfidf(List<Doc> docs) {
        Index index = new Index(docs);

        docs.forEach(doc -> {
            HashMap<String, Double> frequencies = new HashMap<>();

            doc.getTokens().parallelStream()
                    .filter(token -> !frequencies.containsKey(token.getValue()))
                    .forEach(token -> {
                        double tfidf = getTermFrequency(token, doc) * getInverseTermFrequency(token, docs);
                        frequencies.put(token.getValue(), tfidf);

                        token.setFrequence(tfidf);
                    });
            doc.setFrequencies(frequencies);
        });

        return index;
    }

}
