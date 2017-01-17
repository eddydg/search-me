package main;

import main.Models.Doc;
import main.Models.Index;
import main.Models.Result;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        logger.trace("Start app");

        String url = "https://en.wikipedia.org/wiki/Lidar";


        BlockingQueue<Doc> crawledDocs = new LinkedBlockingDeque<>();
        Index index = new Index();
        Indexer indexer = new Indexer(index, crawledDocs);

        try {
            Crawler crawler = new Crawler(new URL(url), crawledDocs);
            Thread crawlerThread = new Thread(crawler);
            crawlerThread.start();

            Thread indexerThread = new Thread(indexer);
            indexerThread.start();
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Requester requester = new Requester(index, 5);
        requester.prompt();


        long endTime = System.currentTimeMillis();
        logger.trace("End app ({}ms)", (endTime - startTime));
    }

}
