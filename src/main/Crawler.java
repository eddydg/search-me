package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Crawler {

    static public String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";

    public static void crawler(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            doc.select("a").forEach((e) -> {
                String href = e.attr("href");
                if (href.matches(URL_MATCH_REGEX)) {
                    System.out.println("Absolute link: " + href);
                } else {
                    String separator = href.startsWith("/") ? "" : "/";
                    System.out.println("Relative link: " + url + separator + href);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
