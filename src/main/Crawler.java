package main;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Crawler {

    static int MAX_DOCUMENTS = 10;
    static private String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";
    static private List<URL> crawledURLs = new ArrayList<>();

    public static List<URL> crawler(URL url) {
        BlockingQueue<URL> urls = new LinkedBlockingDeque<>(Arrays.asList(url));

        while(crawledURLs.size() < MAX_DOCUMENTS && !urls.isEmpty()){

            URL currentUrl = null;
            try {
                currentUrl = urls.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return crawledURLs;
            }

            if (crawledURLs.contains(currentUrl))
                continue;

            try {
                Connection.Response res = Jsoup.connect(currentUrl.toString()).execute();
                if (res.contentType().contains("text/html")) {
                    Document doc = res.parse();
                    Main.logger.trace("{} ({}/{})", currentUrl, crawledURLs.size() + 1, MAX_DOCUMENTS);
                    crawledURLs.add(currentUrl);

                    for (Element e: doc.select("a")){
                        URL newUrl = getProperUrl(url, e);
                        if (newUrl != null)
                            urls.add(newUrl);
                    }
                }
            } catch (IOException e) {
                Main.logger.error("Jsoup connect error on " + currentUrl.toString());
            }
        }

        return crawledURLs;
    }

    private static URL getProperUrl(URL url, Element e) {
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

        return newUrl;
    }
}
