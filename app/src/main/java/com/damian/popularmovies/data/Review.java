/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.data;

/**
 * Provides an abstraction for Review data.
 * For use with the Movie class.
 */

public class Review {

    private String mAuthor;
    private String mContent;

    public Review(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }
}
