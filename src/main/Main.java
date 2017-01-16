package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Result;
import main.Models.Token;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        logger.trace("Start app");


        String url = "https://en.wikipedia.org/wiki/Lidar";
        Index index = null;
        try {
            Crawler crawler = new Crawler(new URL(url));
            crawler.run();
            List<URL> crawledUrls = crawler.getCrawledURLs();
            index = Indexer.run(crawledUrls.stream());
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
        }

        if (index != null) {
            Requester requester = new Requester(index);
            List<Result> results = requester.search("lidar");

            results.forEach(System.out::println);
        }

        long endTime = System.currentTimeMillis();
        logger.trace("End app ({}ms)", (endTime - startTime));
    }

}
