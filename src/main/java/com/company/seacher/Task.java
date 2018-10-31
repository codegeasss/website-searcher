package com.company.seacher;

/**
 * Represents a single task with web url and its corresponding line number from the urls file
 */
public class Task {

    private String webUrl;
    private int lineNum;

    public Task(String webUrl, int lineNum) {
        this.webUrl = webUrl;
        this.lineNum = lineNum;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return "Task{" +
                "webUrl='" + webUrl + '\'' +
                ", lineNum=" + lineNum +
                '}';
    }
}
