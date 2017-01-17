package main;

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
class Crawler {

    private final int MAX_DOCUMENTS = 20;
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

    void run() {
        double startTime = System.currentTimeMillis();

        // FIXME: let first Task fill `urls` otherwise next Tasks won't wait on empty list
        Thread initThread = new Thread(new CrawlTask(urls, crawledURLs, 0, true));
        initThread.run();
        try {
            initThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ExecutorService es = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < poolSize; i++) {
            CrawlTask ct = new CrawlTask(urls, crawledURLs, i + 1);
            es.execute(ct);
        }

        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double endTime = System.currentTimeMillis();
        Main.logger.trace("End Crawler ({}ms)", (endTime - startTime));
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
        final List<URL> crawledURLs;
        int threadNumber;
        boolean isFirst;

        CrawlTask(BlockingQueue<URL> urls, List<URL> crawledURLs, int threadNumber) {
            this.urls = urls;
            this.crawledURLs = crawledURLs;
            this.threadNumber = threadNumber;
            this.isFirst = false;
        }

        CrawlTask(BlockingQueue<URL> urls, List<URL> crawledURLs, int threadNumber, boolean isFirst) {
            this.urls = urls;
            this.crawledURLs = crawledURLs;
            this.threadNumber = threadNumber;
            this.isFirst = isFirst;
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

                if (isFirst) break;
            }
        }

        // TODO: instead of filling with URLs, fill with Document with already set Content (avoid doing it in Indexer)
        void crawl(URL url) {
            try {
                Connection.Response res = Jsoup.connect(url.toString()).execute();
                if (res.contentType().contains("text/html")) {
                    Document doc = res.parse();

                    if (crawledURLs.size() >= MAX_DOCUMENTS) return;

                    Main.logger.trace("Crawled {} ({}/{})", url, crawledURLs.size() + 1, MAX_DOCUMENTS);
                    crawledURLs.add(url);

                    for (Element e: doc.select("a")){
                        URL newUrl = getProperUrl(url, e.attr("href"));
                        if (newUrl != null && !crawledURLs.contains(newUrl) && !urls.contains(newUrl))
                            urls.add(newUrl);
                    }
                }
            } catch (IOException e) {
                Main.logger.error("Jsoup connect error on " + url.toString());
            }
        }
    }

    List<URL> getCrawledURLs() {
        return crawledURLs;
    }
}
