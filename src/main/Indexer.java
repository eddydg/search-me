package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Indexer {

    private static String STOP_WORDS_FILE = "assets/stops.en.txt";
    private static List<String> suffixesToStrip = new ArrayList<>(Arrays.asList("ed", "ing", "ly"));

    public HashMap<String, Integer> index;

    public static String fetchBody(URL url) {
        String content = "";
        try {
            Document doc = Jsoup.connect(url.toString()).get();
            content = doc.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String cleanup(String input) {
        Document doc = Jsoup.parse(input);
        return doc.text();
    }

    public static Stream<String> tokenize(String input) {
        String[] words = input.toLowerCase().split("\\W+");

        File file = new File(STOP_WORDS_FILE);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            Set<String> stopWords = new HashSet<>(Arrays.asList(new String(data, "UTF-8").split("\n")));
            return Arrays.stream(words).filter(w -> !stopWords.contains(w));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stream.Builder<String> b = Stream.builder();
        return b.build();
    }

    public static String reduce(String input) {
        for (String s: suffixesToStrip) {
            if (input.endsWith(s)) {
                return input.substring(0, input.length() - s.length());
            }
        }

        return input;
    }

    public static long getWordFrequency(String word, List<String> input) {
        return Collections.frequency(input, word);
    }

    public static long getInverseWordsFrequencies(String word, List<List<String>> input) {
        long d = input.size();
        if (d == 0) return 0;
        long count = input.parallelStream().filter(doc -> doc.contains(word)).count();

        return d / count;
    }

    public static Map<String, Double> tfidf(List<List<String>> input) {
        Map<String, Double> frequencies = new HashMap<>();

        for (List<String> doc: input) {
            for (String word: doc) {
                if (!frequencies.containsKey(word)) {
                    double v = Math.log(getWordFrequency(word, doc) * getInverseWordsFrequencies(word, input));
                    frequencies.put(word, v);
                }
            }
        }

        return frequencies;
    }

}
