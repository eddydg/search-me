package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Crawler {

    static int MAX_LEVEL = 3;
    static private String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";
    static private List<URL> urls;

    public static List<URL> crawler(String url) {
        urls = new ArrayList<>();
        crawler(url, MAX_LEVEL);
        return urls;
    }

    private static void crawler(String url, int maxLevel) {
        if (maxLevel <= 0) return;

        try {
            Document doc = Jsoup.connect(url).get();
            doc.select("a").forEach((e) -> {
                String href = e.attr("href");
                URL newUrl = null;

                // Absolute link
                if (href.matches(URL_MATCH_REGEX)) {
                    try {
                        newUrl = new URL(href);
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                    Main.logger.trace("Absolute link: " + href);
                }
                // Relative link
                else {
                    try {
                        newUrl = new URL(new URL(url), href);
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                    Main.logger.trace("Relative link: " + newUrl);
                }

                if (newUrl != null && !urls.contains(newUrl)) {
                    urls.add(newUrl);
                    crawler(newUrl.toString(), maxLevel - 1);
                }
            });
        } catch (Exception e) {
            Main.logger.error(url);
            e.printStackTrace();
        }
    }
}
