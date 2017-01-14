package main.Models;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Doc {

    URL url;
    List<Token> tokens;
    String content;

    public Doc() {
        tokens = new ArrayList<>();
    }

    public Doc(URL url, List<Token> tokens, String content) {
        this.url = url;
        this.tokens = tokens;
        this.content = content;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean containsWord(String word) {
        return tokens.stream().anyMatch(token -> token.getValue().equals(word));
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
}
