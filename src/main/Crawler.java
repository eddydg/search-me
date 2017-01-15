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
class Crawler implements Runnable {

    private final int MAX_DOCUMENTS = 50;
    private int poolSize = 5;
    private final String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";

    private final BlockingQueue<URL> urls = new LinkedBlockingDeque<>();
    private final List<URL> crawledURLs = Collections.synchronizedList(new ArrayList<>());

    public Crawler(URL url) throws InterruptedException {
        urls.put(url);
        this.poolSize = 5;
    }

    public Crawler(URL url, int poolSize) throws InterruptedException {
        urls.put(url);
        this.poolSize = poolSize;
    }

    @Override
    public void run() {
        CrawlTask t[] = new CrawlTask[poolSize];

        for (int i = 0; i < poolSize; i++) {
            t[i] = new CrawlTask(urls, crawledURLs);
            Main.logger.info("Starting CrawlTask n°{}", (i + 1));
            t[i].start();
        }

        for (int i = 0; i < poolSize; i++) {
            try {
                t[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private URL getProperUrl(URL url, String href) {
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

    class CrawlTask extends Thread {
        BlockingQueue<URL> urls;
        List<URL> crawledURLs;

        public CrawlTask(BlockingQueue<URL> urls, List<URL> crawledURLs) {
            this.urls = urls;
            this.crawledURLs = crawledURLs;
        }

        @Override
        public void run() {
            while(crawledURLs.size() < MAX_DOCUMENTS && !urls.isEmpty()){
                URL currentUrl;
                try {
                    currentUrl = urls.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                crawl(currentUrl);
            }
        }

        void crawl(URL url) {
            try {
                Connection.Response res = Jsoup.connect(url.toString()).execute();
                if (res.contentType().contains("text/html")) {
                    Document doc = res.parse();
                    Main.logger.trace("{} ({}/{})", url, crawledURLs.size() + 1, MAX_DOCUMENTS);
                    crawledURLs.add(url);

                    for (Element e: doc.select("a")){
                        URL newUrl = getProperUrl(url, e.attr("href"));
                        if (newUrl != null && !crawledURLs.contains(newUrl))
                            urls.add(newUrl);
                    }
                }
            } catch (IOException e) {
                Main.logger.error("Jsoup connect error on " + url.toString());
            }
        }
    }

    public List<URL> getCrawledURLs() {
        return crawledURLs;
    }
}
