package com.company.seacher;


import com.company.seacher.utils.HttpHelper;
import com.company.seacher.utils.Matcher;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * This class implements the crawler thread
 * Once launched, crawler thread listens to incoming queue for new tasks.
 * As soon as a task is available, crawler would fetch the contents from the site and
 * does a search for the term. The result is then queued up in results queue.
 * This thread terminates when a NULL is received from incoming queue, which is a signal from
 * the main thread to terminate
 */
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

    /**
     * Run method which waits on incoming queue for tasks
     * Returns when a NULL value is received in the queue
     */
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

    /**
     * Connects to web url and looks for a match
     *
     * @param task contains term and url details
     * @param matcher test matcher
     * @throws IOException if url is not reachable
     * @return boolean true for a positive match
     */
    private boolean lookup(Task task, Matcher matcher) throws IOException {
        HttpURLConnection httpConnection = HttpHelper.getConnection(task.getWebUrl());
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
}
