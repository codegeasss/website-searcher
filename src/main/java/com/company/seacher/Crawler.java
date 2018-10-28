package com.company.seacher;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Crawler implements Runnable{

    BlockingQueue<Task> incoming;
    BlockingQueue<Result> results;
    String term;

    public Crawler(BlockingQueue<Task> incoming, BlockingQueue<Result> results, String term) {
        this.incoming = incoming;
        this.results = results;
        this.term = term;
    }

    @Override
    public void run() {
        while(true) {
            Task t = null;
            try {
                t = incoming.take();
            } catch (InterruptedException e) {
                System.err.println(String.format("%s interrupted!", Thread.currentThread().getName()));
            }
            if(t != null) {
                boolean found = false;
                String err = "";
                try {
                    found = lookup(t);
                } catch (IOException e) {
                    err = e.getMessage();
                }

                try {
                    results.put(new Result(t, found, err));
                } catch (Throwable e) {
                    System.err.println(String.format("%s interrupted!", Thread.currentThread().getName()));
                }
            } else { // termination condition
                break;
            }
        }
    }

    // TODO Close resources
    // TODO remove sysouts
    private boolean lookup(Task m) throws IOException {
        // try http first
        HttpURLConnection httpConnection = (HttpURLConnection) getConnection("http://" + m.getWebUrl());
        if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // try https
            httpConnection = (HttpsURLConnection) getConnection("https://" + m.getWebUrl());
            // throw exception if the response is still not 200
            if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new UnknownHostException("Unknown host");
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader( httpConnection.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            // exit early if the term is in the current line
            // no need to load the entire website content in memory
            boolean found = Pattern.compile(Pattern.quote(this.term), Pattern.CASE_INSENSITIVE).matcher(line).find();
            if(found) return true;
        }
        return false;
    }

    private URLConnection getConnection(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();;
        conn.setReadTimeout(5000);
        return conn;
    }
}
