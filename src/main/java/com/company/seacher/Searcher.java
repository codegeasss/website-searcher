package com.company.seacher;

import com.company.seacher.utils.HttpHelper;
import com.company.seacher.utils.ThreadManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Entry point of the application responsible for creating tasks from urls file
 * and launching crawlers and result aggregator threads
 * Also makes sure all the threads are terminated to make a clean exit
 */
public class Searcher {

    String fileUrl;
    String resultsUrl;
    String searchTerm;
    BlockingQueue<Task> tasks;
    BlockingQueue<Result> results;

    final static Logger logger = Logger.getLogger(Searcher.class);


    public Searcher(String fileUrl, String resultsUrl, String searchTerm) {
        this.fileUrl = fileUrl;
        this.resultsUrl = resultsUrl;
        this.searchTerm = searchTerm;
        // bounded queues so we don't use up all the memory for large inputs
        tasks = new BlockingQueue<>(100);
        results = new BlockingQueue<>(100);
    }

    /**
     * Launches 20 crawlers and a result aggregator and starts adding tasks to
     * queue for each url from the urls file
     * Sends shut down signal to both tasks and results queue after all url's are converted to tasks
     */
    public void search() {
        logger.info("Initiating search for term: " + searchTerm);
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

            logger.info("Launching single result aggregator");
            aggrThreadManager.launch(1);

            // launch crawlers to start processing urls
            // as soon as they are available in the queue
            crawlerThreadManager = new ThreadManager<>(
                    () ->  new Crawler(tasks, results, this.searchTerm),
                    "Crawler"
            );

            logger.info("Launching 20 crawlers");
            crawlerThreadManager.launch(20);

            generateTasks();

        } catch (Exception e){
            err = e.getMessage();
        } finally { // all the shutdown/cleanup activities in the finally block
            // done with tasks
            // send termination signal to tasks queue
            logger.info("Terminating tasks queue to signal crawlers to shutdown");
            tasks.terminate();

            // wait for all crawlers to finish
            logger.info("Waiting for crawlers to finish processing");
            if(crawlerThreadManager != null) {
                try {
                    crawlerThreadManager.join();
                } catch (InterruptedException e) {
                    logger.error("Crawler thread interrupted while waiting to join - " + e.getMessage());
                }
            }
            logger.info("Successfully terminated all crawler threads");

            // done with results
            // send termination signal to results queue
            logger.info("Terminating results queue to signal aggregator to shutdown");
            results.terminate();

            // wait for result aggregator to finish
            logger.info("Waiting for aggregator to finish processing");
            if(aggrThreadManager != null) {
                try {
                    aggrThreadManager.join();
                } catch (InterruptedException e) {
                    logger.error("Result aggregator thread interrupted while waiting to join - " + e.getMessage());
                }
            }
            logger.info("Successfully terminated aggregator thread");

            if(err == "")
                logger.info("Search complete. Check " + resultsUrl + " for summary!");
            else
                logger.info("Search failed with error - " + err);
        }

    }

    private void generateTasks() throws IOException {
        logger.info("Reading web urls from file: " + fileUrl);
        HttpURLConnection conn =  HttpHelper.getConnection(this.fileUrl);
        // auto close resources
        try(
                InputStreamReader is = new InputStreamReader(conn.getInputStream());
                CSVReader reader = new CSVReaderHeaderAware(is);
        ) {

            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    // add urls to be processed in task queue
                    tasks.put(new Task(nextLine[1], Integer.valueOf(nextLine[0])));
                }  catch (InterruptedException e) {
                    logger.error("Main thread interrupted - " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Failed to read url details from line - " + nextLine + " : " + e.getMessage());
                }
            }
        }
        logger.info("Done reading web urls from file: " + fileUrl);
    }

}
