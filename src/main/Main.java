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

        /*
        String url = "https://en.wikipedia.org/wiki/Lidar";
        List<URL> crawledUrls;
        try {
            Crawler crawler = new Crawler(new URL(url));
            crawler.run();
            crawledUrls = crawler.getCrawledURLs();
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
        Index index = Indexer.run(crawledUrls.stream());
        */

        List<String> contents = new ArrayList<>(Arrays.asList(
                "Lorem ipsum dolor ipsum sit ipsum",
                "Vituperata incorrupte at ipsum pro quo",
                "Has persius disputationi id simul"
        ));
        Indexer indexer = new Indexer();
        Index index = indexer.run(contents);

        if (index != null) {
            Requester requester = new Requester(index);
            List<Result> results = requester.search("simul");

            results.forEach(System.out::println);
        }

        long endTime = System.currentTimeMillis();
        logger.trace("End app ({}ms)", (endTime - startTime));
    }

}
