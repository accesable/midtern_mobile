package com.tdtu.myapplication.service;

import androidx.appcompat.widget.SearchView;

public interface SearchProcessor {
    void processSearch(String query);
    void clearTextView(SearchView searchView);

}
