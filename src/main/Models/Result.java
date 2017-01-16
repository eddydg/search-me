package main.Models;

/**
 * Created by MeltedPenguin on 16/01/2017.
 */
public class Result {

    private Doc doc;
    private double score;

    public Result(Doc doc) {
        this.doc = doc;
    }

    public Result(Doc doc, double score) {
        this.doc = doc;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Result{" +
                "url=" + doc.getUrl() +
                ", score=" + score +
                '}';
    }

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
