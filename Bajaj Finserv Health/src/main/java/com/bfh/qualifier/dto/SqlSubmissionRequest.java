package com.bfh.qualifier.dto;

public class SqlSubmissionRequest {
    private String finalQuery;

    public SqlSubmissionRequest() {}

    public SqlSubmissionRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}

