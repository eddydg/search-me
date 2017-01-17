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
class Crawler {

    private final int MAX_DOCUMENTS = 20;
    private int poolSize = 10;
    private final String URL_MATCH_REGEX = "((http(s)?://.)|(www\\.)).*";

    private final BlockingQueue<URL> urls = new LinkedBlockingDeque<>();
    private final List<Doc> crawledDocs = Collections.synchronizedList(new ArrayList<>());

    public Crawler(URL url) throws InterruptedException {
        urls.put(url);
    }

    public Crawler(URL url, int poolSize) throws InterruptedException {
        urls.put(url);
        this.poolSize = poolSize;
    }

    void run() {
        double startTime = System.currentTimeMillis();

        ExecutorService es = Executors.newFixedThreadPool(poolSize);

        int runningThreads = 1;
        do {
            if (runningThreads < poolSize) {
                CrawlTask ct = new CrawlTask(urls, crawledDocs, 0);
                es.execute(ct);
            }

            if (urls.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runningThreads = ((ThreadPoolExecutor) es).getActiveCount();
        } while (runningThreads > 0);

        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double endTime = System.currentTimeMillis();
        Main.logger.info("End Crawler ({}ms)", (endTime - startTime));
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
        final List<Doc> crawledDocs;
        int threadNumber;
        boolean isFirst;

        CrawlTask(BlockingQueue<URL> urls, List<Doc> crawledDocs, int threadNumber) {
            this.urls = urls;
            this.crawledDocs = crawledDocs;
            this.threadNumber = threadNumber;
            this.isFirst = false;
        }

        CrawlTask(BlockingQueue<URL> urls, List<Doc> crawledDocs, int threadNumber, boolean isFirst) {
            this.urls = urls;
            this.crawledDocs = crawledDocs;
            this.threadNumber = threadNumber;
            this.isFirst = isFirst;
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

                if (isFirst) break;
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

    List<Doc> getCrawledURLs() {
        return crawledDocs;
    }
}
