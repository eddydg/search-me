package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Token;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
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
        return new Doc(url, null, content);
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
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            Set<String> stopWords = new HashSet<>(Arrays.asList(new String(data, "UTF-8").split("\n")));

            List<Token> tokens = IntStream.range(0, words.length)
                    .filter(i -> !stopWords.contains(words[i]))
                    .mapToObj(i -> new Token(words[i], 0, i))
                    .collect(Collectors.toList());

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

    public static long getWordFrequency(Token token, Doc input) {
        return input.getTokens().stream().filter(t -> t.getValue().equals(token.getValue())).count();
    }

    public static double getInverseWordsFrequencies(Token token, List<Doc> input) {
        long d = input.stream().map(doc -> doc.getTokens().size()).count();
        if (d == 0) return 0;
        long count = input.parallelStream().filter(doc -> doc.containsWord(token.getValue())).count();

        return 1 + Math.log(d / count);
    }

    public static Index tfidf(List<Doc> docs) {
        HashMap<String, Double> frequencies = new HashMap<>();
        Index index = new Index(docs, frequencies);

        docs.forEach(doc ->
            doc.getTokens().parallelStream()
                    .filter(token -> !frequencies.containsKey(token.getValue()))
                    .forEach(token -> {
                        double f;
                        if (frequencies.containsKey(token.getValue()))
                            f = frequencies.get(token.getValue());
                        else {
                            f = Math.log(getWordFrequency(token, doc) * getInverseWordsFrequencies(token, docs));
                            frequencies.put(token.getValue(), f);
                        }
                        token.setFrequence(f);
                    })
        );
        index.setFrequencies(frequencies);

        return index;
    }

}
