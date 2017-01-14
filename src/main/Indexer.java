package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    public static List<String> tokenize(String input) {
        String[] words = input.toLowerCase().split("\\W+");
        List<String> res = new ArrayList<>(Arrays.asList(words));

        File file = new File(STOP_WORDS_FILE);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            Set<String> stopWords = new HashSet<>(Arrays.asList(new String(data, "UTF-8").split("\n")));
            res = res.stream().filter(w -> !stopWords.contains(w)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static String reduce(String input) {
        for (String s: suffixesToStrip) {
            if (input.endsWith(s)) {
                return input.substring(0, input.length() - s.length());
            }
        }

        return input;
    }

}
