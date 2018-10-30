package com.company.seacher;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Searcher {

    String fileUrl;
    String resultsUrl;
    String searchTerm;
    BlockingQueue<Task> tasks;
    BlockingQueue<Result> results;


    public Searcher(String fileUrl, String resultsUrl, String searchTerm) {
        this.fileUrl = fileUrl;
        this.resultsUrl = resultsUrl;
        this.searchTerm = searchTerm;
        // bounded queues so we don't use up all the memory for large inputs
        tasks = new BlockingQueue<>(100);
        results = new BlockingQueue<>(100);
    }

    public void search() {
        System.out.println("Initiating search for term: " + searchTerm);
        ThreadManager<ResultAggregator> aggrThreadManager = null;
        ThreadManager<Crawler> crawlerThreadManager = null;
        String err = "";
        try {
            // launch result aggregator start aggregating results
            // as soon as they are available in the queue
            aggrThreadManager = new ThreadManager<>(
                    () ->  new ResultAggregator(this.results, this.resultsUrl),
                    "ResultAggregator"
            );
            System.out.println("Launching single result aggregator");
            aggrThreadManager.launch(1);

            // launch crawlers to start processing urls
            // as soon as they are available in the queue
            crawlerThreadManager = new ThreadManager<>(
                    () ->  new Crawler(tasks, results, this.searchTerm),
                    "Crawler"
            );
            System.out.println("Launching 20 crawlers");
            crawlerThreadManager.launch(20);

            generateTasks();

        } catch (Exception e){
            err = e.getMessage();
        } finally { // all the shutdown/cleanup activities in the finally block
            // done with tasks
            // send termination signal to tasks queue
            System.out.println("Terminating tasks queue to signal crawlers to shutdown");
            tasks.terminate();
            // wait for all crawlers to finish
            System.out.println("Waiting for crawlers to finish processing");
            if(crawlerThreadManager != null) {
                try {
                    crawlerThreadManager.join();
                } catch (InterruptedException e) {
                    System.err.println("Crawler thread interrupted while waiting to join - " + e.getMessage());
                }
            }
            System.out.println("Successfully terminated all crawler threads");
            // done with results
            // send termination signal to results queue
            System.out.println("Terminating results queue to signal aggregator to shutdown");
            results.terminate();
            // wait for result aggregator to finish
            System.out.println("Waiting for aggregator to finish processing");
            if(aggrThreadManager != null) {
                try {
                    aggrThreadManager.join();
                } catch (InterruptedException e) {
                    System.err.println("Result aggregator thread interrupted while waiting to join - " + e.getMessage());
                }
            }
            System.out.println("Successfully terminated aggregator thread");
            if(err == "")
                System.out.println("Search complete. Check " + resultsUrl + " for summary!");
            else
                System.err.println("Search failed with error - " + err);
        }

    }

    private void generateTasks() throws IOException {
        System.out.println("Reading web urls from file: " + fileUrl);
        HttpsURLConnection conn = (HttpsURLConnection) new URL(this.fileUrl).openConnection();
        conn.setReadTimeout(5000);
        // auto close resources
        try(
                InputStreamReader is = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(is);
        ) {
            String line;
            int lineNum = -1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                // skip header
                if(lineNum == 0) continue;
                try {
                    String webUrl = extractWebUrl(line);
                    // add urls to be processed in task queue
                    tasks.put(new Task(webUrl, lineNum));
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                } catch (InterruptedException e) {
                    System.err.println("Main thread interrupted - " + e.getMessage());
                }
            }
        }
        System.out.println("Done reading web urls from file: " + fileUrl);
    }


    private String extractWebUrl(String line) throws IllegalArgumentException {
        if(line != null) {
            String[] cols = line.split(",");
            if(cols.length >= 2) {
                return cols[1];
            }
        }
        throw new IllegalArgumentException("Failed to extract web url. Invalid line - " + line);
    }

}
