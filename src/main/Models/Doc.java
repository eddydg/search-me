package main.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Doc {

    List<Token> tokens;

    public Doc() {
        tokens = new ArrayList<>();
    }

    public Doc(List<Token> tokens) {
        this.tokens = tokens;
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
}
