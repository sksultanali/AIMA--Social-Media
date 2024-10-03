package com.developerali.aima.Models;

public class RecentSearchModel {

    String  search_query, type;

    public RecentSearchModel(String search_query, String type) {
        this.search_query = search_query;
        this.type = type;
    }

    public RecentSearchModel() {
    }

    public String getSearch_query() {
        return search_query;
    }

    public void setSearch_query(String search_query) {
        this.search_query = search_query;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
