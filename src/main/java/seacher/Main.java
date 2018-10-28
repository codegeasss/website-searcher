package main.java.seacher;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Searcher s = new Searcher("https://s3.amazonaws.com/fieldlens-public/urls.txt", "results.txt", "technology");
        try {
            s.search();
        } catch (IOException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
    }
}
