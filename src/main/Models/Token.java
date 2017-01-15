package main.Models;

/**
 * Created by MeltedPenguin on 14/01/2017.
 */
public class Token {

    private String value;
    private double frequence;
    private long position;

    public Token(String value, long frequence, long position) {
        this.value = value;
        this.frequence = frequence;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public double getFrequence() {
        return frequence;
    }

    public void setFrequence(double frequence) {
        this.frequence = frequence;
    }
}
