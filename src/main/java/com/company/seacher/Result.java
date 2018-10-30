package com.company.seacher;

public class Result extends Task {

    private boolean found;
    private String error;

    public Result(Task t, boolean found) {
        super(t.getWebUrl(), t.getLineNum());
        this.found = found;
        this.error = error;
    }

    public Result(Task t, boolean found, String error) {
        this(t, found);
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
                .append(",").append(found).append(",");
        if(error != null && error != "") {
            sb.append("Search Failed: " + error);
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
