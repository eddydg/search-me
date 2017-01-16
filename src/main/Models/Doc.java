package main.Models;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Doc {

    private URL url;
    private List<Token> tokens;
    private String content;
    private HashMap<String, Double> frequencies;

    public Doc() {
        this.tokens = new ArrayList<>();
        this.frequencies = new HashMap<>();
    }

    public Doc(URL url, List<Token> tokens, String content, HashMap frequencies) {
        this.url = url;
        this.tokens = tokens;
        this.content = content;
        this.frequencies = frequencies;
    }

    @Override
    public String toString() {
        return "Doc{" +
                "url=" + url +
                ", tokens=" + tokens +
                ", content='" + content + '\'' +
                ", frequencies=" + frequencies +
                '}';
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean containsWord(String word) {
        return tokens.parallelStream().anyMatch(token -> token.getValue().equalsIgnoreCase(word));
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public HashMap<String, Double> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(HashMap<String, Double> frequencies) {
        this.frequencies = frequencies;
    }
}
