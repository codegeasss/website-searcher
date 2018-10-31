package com.company.seacher;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Thread implementation for result aggregator
 * As the name suggests, this thread listens to results queue and writes
 * to results file as soon as one is available
 * This thread terminates when a NULL is received from results queue, which is a signal from
 * the main thread to terminate
 */
public class ResultAggregator implements Runnable{

    private BlockingQueue<Result> results;
    private String resultFilePath;

    final static Logger logger = Logger.getLogger(ResultAggregator.class);

    public ResultAggregator(BlockingQueue<Result> results, String resultFilePath) {
        this.results = results;
        this.resultFilePath = resultFilePath;
    }

    /**
     * Run method which waits on results queue for task results
     * Returns when a NULL value is received in the queue
     */
    @Override
    public void run() {
        while(true) {
            Result r = null;
            try {
                r = results.take();
            } catch (InterruptedException e) {
                logger.error(String.format("%s interrupted!", Thread.currentThread().getName()));
            }
            if(r != null) {
                writeResult(r);
            } else { // termination condition
                break;
            }
        }
    }

    /**
     * Writes results to the results file
     * New file is created if results file doesn't exist
     * Otherwise results are appended to the existing file
     *
     * @param   r Result object
     */
    private void writeResult(Result r) {
        try {
            Files.write(Paths.get(resultFilePath), r.print().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write result " + r + " - " + e.getMessage());
        }
    }
}
