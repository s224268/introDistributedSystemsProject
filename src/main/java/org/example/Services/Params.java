package org.example.Services;

import java.util.StringJoiner;

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

    public String toQueryString() {
        StringJoiner joiner = new StringJoiner("&");
        if (strict != null) joiner.add("strict=" + strict);
        if (limit != null) joiner.add("limit=" + limit);
        if (matchCase != null) joiner.add("matchCase=" + matchCase);
        if (scrapeType != null) joiner.add("scrapeType=" + scrapeType);
        if (page != null) joiner.add("page=" + page);
        if (multiPage != null) joiner.add("multiPage=" + multiPage);
        return joiner.toString();
    }
}