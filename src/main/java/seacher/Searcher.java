package main.java.seacher;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Searcher {

    String fileUrl;
    String resultsUrl;
    String searchTerm;
    BlockingQueue<Task> tasks;
    BlockingQueue<Result> results;

    private static final Pattern URL_PATTERN = Pattern.compile("\\d+,\"(.*?)\",.*");


    public Searcher(String fileUrl, String resultsUrl, String searchTerm) {
        this.fileUrl = fileUrl;
        this.resultsUrl = resultsUrl;
        this.searchTerm = searchTerm;
        // bounded queues so we don't use up all the memory for large inputs
        tasks = new BlockingQueue<>(100);
        results = new BlockingQueue<>(100);
    }

    public void search() throws IOException{
        System.out.println("Initiating search for term: " + searchTerm);
        HttpsURLConnection conn = (HttpsURLConnection) new URL(this.fileUrl).openConnection();;
        conn.setReadTimeout(15000);
        BufferedReader br = new BufferedReader(new InputStreamReader( conn.getInputStream()));
        // launch result aggregator start aggregating results
        // as soon as they are available in the queue
        ThreadManager<ResultAggregator> aggrThreadManager = new ThreadManager<>(
                () ->  new ResultAggregator(this.results, this.resultsUrl),
                "ResultAggregator"
        );
        System.out.println("Launching single result aggregator");
        aggrThreadManager.launch(1);

        // launch crawlers to start processing urls
        // as soon as they are available in the queue
        ThreadManager<Crawler> crawlerThreadManager = new ThreadManager<>(
                () ->  new Crawler(tasks, results, this.searchTerm),
                "Crawler"
        );
        System.out.println("Launching 20 crawlers");
        crawlerThreadManager.launch(20);

        System.out.println("Reading web urls from file: " + fileUrl);
        String line;
        int lineNum = 0;
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
        // done with tasks
        // send termination signal to tasks queue
        tasks.terminate();
        // wait for all crawlers to finish
        try {
            crawlerThreadManager.join();
        } catch (InterruptedException e) {
            System.err.println("Crawler thread interrupted while waiting to join - " + e.getMessage());
        }
        // done with results
        // send termination signal to results queue
        results.terminate();
        // wait for result aggregator to finish
        try {
            aggrThreadManager.join();
        } catch (InterruptedException e) {
            System.err.println("Result aggregator thread interrupted while waiting to join - " + e.getMessage());
        }
    }

    private List<Thread> launchCrawlers() {
        List<Thread> crawlers = new ArrayList<>();
        System.out.println("Launching 20 crawlers");
        for(int i = 1; i <= 20; i++) {
            Crawler c = new Crawler(tasks, results, this.searchTerm);
            Thread t = new Thread(c, "Crawler" + i);
            crawlers.add(t);
            t.start();
        }
        return crawlers;
    }

    private void launchResultAggregator() {
        System.out.println("Launching result aggregator");
        ResultAggregator ra = new ResultAggregator(this.results, this.resultsUrl);
        Thread t = new Thread(ra, "AggregatorThread");
        t.start();
    }

    // TODO use csv parser
    private String extractWebUrl(String line) throws IllegalArgumentException {
        Matcher m = URL_PATTERN.matcher(line);
        if(!m.find() || m.groupCount() < 1)
            throw new IllegalArgumentException("Failed to extract web url. Invalid line - " + line);
        return m.group(1);
    }

}
