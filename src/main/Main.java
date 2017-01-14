package main;

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
        logger.trace("Start app");

        List<URL> urls = null;
        try {
            urls = Crawler.crawler(new URL("https://en.wikipedia.org/wiki/Lidar"));
            Map<String, Double> index = Indexer.run(urls.stream());
            index.forEach((k,v) -> System.out.println(k + " = " + v));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        logger.trace("End app");
    }
}
