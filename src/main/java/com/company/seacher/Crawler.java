package com.company.seacher;


import com.company.seacher.utils.HttpHelper;
import com.company.seacher.utils.Matcher;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Crawler implements Runnable{

    BlockingQueue<Task> incoming;
    BlockingQueue<Result> results;
    String term;

    final static Logger logger = Logger.getLogger(Crawler.class);

    public Crawler(BlockingQueue<Task> incoming, BlockingQueue<Result> results, String term) {
        this.incoming = incoming;
        this.results = results;
        this.term = term;
    }

    @Override
    public void run() {
        Matcher matcher = new Matcher(term);
        while(true) {
            Task t = null;
            try {
                t = incoming.take();
            } catch (InterruptedException e) {
                logger.error(String.format("%s interrupted!", Thread.currentThread().getName()));
            }
            if(t != null) {
                boolean found = false;
                String err = "";
                try {
                    found = lookup(t, matcher);
                } catch (IOException e) {
                    err = e.getMessage();
                }

                try {
                    results.put(new Result(t, term, found, err));
                } catch (InterruptedException e) {
                    logger.error(String.format("%s interrupted!", Thread.currentThread().getName()));
                }
            } else { // termination condition
                break;
            }
        }
    }


    private boolean lookup(Task m, Matcher matcher) throws IOException {
        HttpURLConnection httpConnection = HttpHelper.getConnection(m.getWebUrl());
        // auto close resources
        try(
                InputStreamReader is = new InputStreamReader(httpConnection.getInputStream());
                BufferedReader br = new BufferedReader(is);
        ) {

            String line;
            while ((line = br.readLine()) != null) {
                // exit early if the term is in the current line
                // no need to load the entire website content in memory
                if (matcher.isMatch(line)) return true;
            }
        }
        return false;
    }

    private URLConnection getConnection(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();;
        conn.setReadTimeout(5000);
        return conn;
    }
}
