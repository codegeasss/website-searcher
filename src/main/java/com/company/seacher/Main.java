package com.company.seacher;

public class Main {
    public static void main(String[] args) {
        Searcher s = new Searcher("https://s3.amazonaws.com/fieldlens-public/urls.txt", "results.txt", "technology");
        s.search();
    }
}
