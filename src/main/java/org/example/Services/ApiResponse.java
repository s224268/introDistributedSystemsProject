package org.example.Services;

import java.util.List;

public class ApiResponse {
    private int statusCode;
    private boolean found;
    private Params params;
    private int totalPages;
    private List<WordDefinition> data;

    // Getters and setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<WordDefinition> getData() {
        return data;
    }

    public void setData(List<WordDefinition> data) {
        this.data = data;
    }
}