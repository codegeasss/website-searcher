package com.company.seacher;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ResultAggregator implements Runnable{

    private BlockingQueue<Result> results;
    private String resultFilePath;

    final static Logger logger = Logger.getLogger(ResultAggregator.class);

    public ResultAggregator(BlockingQueue<Result> results, String resultFilePath) {
        this.results = results;
        this.resultFilePath = resultFilePath;
    }

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

    private void writeResult(Result r) {
        try {
            Files.write(Paths.get(resultFilePath), r.print().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
