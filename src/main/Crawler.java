package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Crawler {

    static int MAX_DOCUMENTS = 500;
    static private String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";
    static private List<URL> urls = new ArrayList<>();

    public static List<URL> crawler(URL url) {
        ListIterator<URL> iter = new ArrayList<>(Arrays.asList(url)).listIterator();
        int count = 0;

        while(count < MAX_DOCUMENTS && iter.hasNext()){
            count++;
            URL currentUrl = iter.next();

            Document doc = null;
            try {
                doc = Jsoup.connect(currentUrl.toString()).ignoreContentType(true).get();
                if (!urls.contains(currentUrl))
                    urls.add(currentUrl);
            } catch (IOException e) {
                Main.logger.error("Jsoup connect error on " + currentUrl.toString());
                continue;
            }

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
                }
                // Relative link
                else {
                    try {
                        newUrl = new URL(url, href);
                    } catch (MalformedURLException e1) {
                        Main.logger.warn("MalformedURLException: " + url + href);
                    }
                }
                Main.logger.trace("{}", newUrl);

                iter.add(newUrl);
            });
        }

        return urls;
    }
}
