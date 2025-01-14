package org.example.Services;

public class Params {
    private String strict;
    private String limit;
    private String matchCase;
    private String scrapeType;
    private String page;
    private String multiPage;

    // Getters and setters
    public String getStrict() {
        return strict;
    }

    public void setStrict(String strict) {
        this.strict = strict;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getMatchCase() {
        return matchCase;
    }

    public void setMatchCase(String matchCase) {
        this.matchCase = matchCase;
    }

    public String getScrapeType() {
        return scrapeType;
    }

    public void setScrapeType(String scrapeType) {
        this.scrapeType = scrapeType;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getMultiPage() {
        return multiPage;
    }

    public void setMultiPage(String multiPage) {
        this.multiPage = multiPage;
    }
}