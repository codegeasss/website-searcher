package com.company.seacher;

public class App {
    public static void main(String[] args) {
        Searcher s = new Searcher("https://s3.amazonaws.com/fieldlens-public/urls.txt",
                "results.txt", "halloween");
        s.search();
    }
}
