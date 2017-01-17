package main;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import main.Models.Doc;
import main.Models.Index;
import main.Models.Result;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        logger.trace("Start app");

        Indexer indexer = new Indexer();

        String url = "https://en.wikipedia.org/wiki/Lidar";
        List<Doc> crawledUrls;
        try {
            Crawler crawler = new Crawler(new URL(url));
            crawler.run();
            crawledUrls = crawler.getCrawledURLs();
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Index index = indexer.run(crawledUrls.stream());
        if (index != null) {
            Requester requester = new Requester(index);
            List<Result> results = requester.search("lidar");

            results.forEach(System.out::println);
        }

        long endTime = System.currentTimeMillis();
        logger.trace("End app ({}ms)", (endTime - startTime));
    }

}
