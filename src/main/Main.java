package main;

import main.Models.Doc;
import main.Models.Index;
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
        try {
            urls = Crawler.crawler(new URL("https://en.wikipedia.org/wiki/Lidar"));
            Index index = Indexer.run(urls.stream());

            for (Token token: index.getDocs().get(0).getTokens())
                System.out.println(token.getPosition() + ": " + token.getValue() + "(" + token.getFrequence() + ")");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        logger.trace("End app ({}ms)", (endTime - startTime));
    }

}
