package main;

import main.Models.Doc;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
class Crawler implements Runnable{

    private final int MAX_DOCUMENTS = 100000;
    private int poolSize = 10;
    private final String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";

    private final BlockingQueue<URL> urls = new LinkedBlockingDeque<>();
    private final BlockingQueue<Doc> crawledDocs;

    public Crawler(URL url, BlockingQueue<Doc> crawledDocs) throws InterruptedException {
        urls.put(url);
        this.crawledDocs = crawledDocs;
    }

    public Crawler(URL url, BlockingQueue<Doc> crawledDocs, int poolSize) throws InterruptedException {
        urls.put(url);
        this.poolSize = poolSize;
         this.crawledDocs = crawledDocs;
    }

    @Override
    public void run() {
        ExecutorService es = Executors.newFixedThreadPool(poolSize);

        do {
            if (((ThreadPoolExecutor) es).getActiveCount() >= poolSize || urls.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                CrawlTask ct = new CrawlTask(urls, crawledDocs, 0);
                es.execute(ct);
            }

        } while (true);
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
        final BlockingQueue<URL> urls;
        final BlockingQueue<Doc> crawledDocs;
        int threadNumber;

        CrawlTask(BlockingQueue<URL> urls, BlockingQueue<Doc> crawledDocs, int threadNumber) {
            this.urls = urls;
            this.crawledDocs = crawledDocs;
            this.threadNumber = threadNumber;
        }

        @Override
        public void run() {
            while(crawledDocs.size() < MAX_DOCUMENTS && !urls.isEmpty()){
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

                    if (crawledDocs.size() >= MAX_DOCUMENTS) return;

                    Main.logger.trace("Crawled {} ({}/{})", url, crawledDocs.size() + 1, MAX_DOCUMENTS);

                    Doc crawledDoc = new Doc(url, null, doc.text(), null);
                    crawledDocs.add(crawledDoc);

                    for (Element e: doc.select("a")){
                        URL newUrl = getProperUrl(url, e.attr("href"));
                        if (newUrl != null &&
                                !crawledDocs.stream().anyMatch(d -> d.getUrl().equals(newUrl)) &&
                                !urls.contains(newUrl)) {
                            urls.add(newUrl);
                        }
                    }
                }
            } catch (IOException e) {
                Main.logger.error("Jsoup connect error on " + url.toString());
            }
        }
    }
}
