package com.company.seacher;

public class Result extends Task {

    String term;
    private boolean found;
    private String error;

    public Result(Task t, String term, boolean found, String error) {
        super(t.getWebUrl(), t.getLineNum());
        this.term = term;
        this.found = found;
        this.error = error;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getLineNum()).append(",").append(this.getWebUrl())
                .append(",").append(this.term).
                append(",").append(found).append(",");
        if(error != null && error != "") {
            sb.append("Search Failed: " + error);
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
