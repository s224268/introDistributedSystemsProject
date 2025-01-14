package org.example.Services;

public class WordDefinition {
    private String word;
    private String meaning;
    private String example;
    private String contributor;
    private String date;

    public WordDefinition(String word, String meaning, String example, String contributor, String date) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.contributor = contributor;
        this.date = date;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Word: " + word + "\nMeaning: " + meaning + "\nExample: " + example + "\nContributor: " + contributor + "\nDate: " + date + "\n----";
    }
}