package main;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        Crawler.crawler("https://fr.wikipedia.org/wiki/Lidar");
        logger.trace("wesh");
    }
}
