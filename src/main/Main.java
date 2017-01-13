package main;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.URL;
import java.util.List;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        //List<URL> urls = Crawler.crawler("https://fr.wikipedia.org/wiki/Lidar");

        String input = "Cette phrase n'est pas à <b>supprimer</b>." +
                "<script>" +
                "Par contre celle-ci, oui." +
                "</script>" +
                "Celle-la non <script>yo</script>plus.";
        String res = Indexer.cleanup(input);
        System.out.println(Indexer.tokenize(res));
    }
}
