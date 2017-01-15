package main;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
class Crawler {

    private static final int MAX_DOCUMENTS = 10;
    private static int POOL_SIZE = 5;
    private static final String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";

    private static final BlockingQueue<URL> urls = new LinkedBlockingDeque<>();
    private static final List<URL> crawledURLs = Collections.synchronizedList(new ArrayList<>());

    static List<URL> run(URL url) {
        urls.add(url);
        while(crawledURLs.size() < MAX_DOCUMENTS && !urls.isEmpty()){

            URL currentUrl;
            try {
                currentUrl = urls.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return crawledURLs;
            }

            if (crawledURLs.contains(currentUrl))
                continue;

            crawl(currentUrl);
        }

        return crawledURLs;
    }

    private static URL getProperUrl(URL url, String href) {
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

    static void crawl(URL url) {
        try {
            Connection.Response res = Jsoup.connect(url.toString()).execute();
            if (res.contentType().contains("text/html")) {
                Document doc = res.parse();
                Main.logger.trace("{} ({}/{})", url, crawledURLs.size() + 1, MAX_DOCUMENTS);
                crawledURLs.add(url);

                for (Element e: doc.select("a")){
                    URL newUrl = getProperUrl(url, e.attr("href"));
                    if (newUrl != null)
                        urls.add(newUrl);
                }
            }
        } catch (IOException e) {
            Main.logger.error("Jsoup connect error on " + url.toString());
        }
    }
}
