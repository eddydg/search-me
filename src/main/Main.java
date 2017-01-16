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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        logger.trace("Start app");


        List<URL> urls = null;
        Index index = null;
        try {
            Crawler crawler = new Crawler(new URL("https://en.wikipedia.org/wiki/Lidar"));
            Thread t = new Thread(crawler);
            t.start();
            t.join();
            index = Indexer.run(crawler.getCrawledURLs().stream());

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
