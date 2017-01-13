package main;

import java.util.HashMap;

/**
 * Created by MeltedPenguin on 13/01/2017.
 */
public class Indexer {

    public static HashMap<String, Integer> index;

    public static String cleanup(String input) {

        StringBuilder sb = new StringBuilder(input);

        String startTag = "<script>";
        String endTag = "</script>";

        int startTagPosition = sb.indexOf(startTag);
        int endTagPosition;
        while(startTagPosition != -1) {
            endTagPosition = sb.indexOf(endTag);
            sb.replace(startTagPosition + startTag.length(), endTagPosition, "");
            sb = new StringBuilder(sb.toString()
                    .replaceFirst(startTag, "")
                    .replaceFirst(endTag, ""));

            startTagPosition = sb.indexOf(startTag);
        }

        return sb.toString();
    }

    public String tokenize(String input) {

        return "";
    }

    public String reduce(String input) {

        return "";
    }

}
