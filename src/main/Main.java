package main;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.trace("Start app");
        //List<URL> urls = Crawler.crawler("https://fr.wikipedia.org/wiki/Lidar");

        String input1 = "Python is a 2000 made-for-TV horror movie directed by Richard\n" +
                "Clabaugh. The film features several cult favorite actors, including William\n" +
                "Zabka of The Karate Kid fame, Wil Wheaton, Casper Van Dien, Jenny McCarthy,\n" +
                "Keith Coogan, Robert Englund (best known for his role as Freddy Krueger in the\n" +
                "A Nightmare on Elm Street series of films), Dana Barron, David Bowe, and Sean\n" +
                "Whalen. The film concerns a genetically engineered snake, a python, that\n" +
                "escapes and unleashes itself on a small town. It includes the classic final\n" +
                "girl scenario evident in films like Friday the 13th. It was filmed in Los Angeles,\n" +
                " California and Malibu, California. Python was followed by two sequels: Python\n" +
                " II (2002) and Boa vs. Python (2004), both also made-for-TV films.";

        String input2 = "Python, from the Greek word (πύθων/πύθωνας), is a genus of\n" +
                "nonvenomous pythons[2] found in Africa and Asia. Currently, 7 species are\n" +
                "recognised.[2] A member of this genus, P. reticulatus, is among the longest\n" +
                "snakes known.";

        String input3 = "The Colt Python is a .357 Magnum caliber revolver formerly\n" +
                "manufactured by Colt's Manufacturing Company of Hartford, Connecticut.\n" +
                "It is sometimes referred to as a \"Combat Magnum\".[1] It was first introduced\n" +
                "in 1955, the same year as Smith &amp; Wesson's M29 .44 Magnum. The now discontinued\n" +
                "Colt Python targeted the premium revolver market segment. Some firearm\n" +
                "collectors and writers such as Jeff Cooper, Ian V. Hogg, Chuck Hawks, Leroy\n" +
                "Thompson, Renee Smeets and Martin Dougherty have described the Python as the\n" +
                "finest production revolver ever made.";

        //String res = Indexer.cleanup(input);
        List<String> doc1 = Indexer.tokenize(input1).collect(Collectors.toList());
        List<String> doc2 = Indexer.tokenize(input2).collect(Collectors.toList());
        List<String> doc3 = Indexer.tokenize(input3).collect(Collectors.toList());

        List<List<String>> docs = new ArrayList<>(Arrays.asList(doc1, doc2, doc3));


        Map<String, Double> frequencies = Indexer.tfidf(docs);
        frequencies.forEach((k, v) -> System.out.println(k + " = " + v));
        System.out.println(frequencies.get("colt"));

        //tokens.forEach(t -> System.out.println(Indexer.reduce(t)));
        logger.trace("End app");
    }
}
