package com.MhamedMalgp.moviesapp.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {

    // content://com.malgp.moviesapp/movie"
    private static final Uri TEST_MOVIE_DIR = MoviesContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER_DIR = MoviesContract.TrailerEntry.CONTENT_URI;
    private static final Uri TEST_REVIEW_DIR = MoviesContract.ReviewEntry.CONTENT_URI;

    private static final Uri TEST_TRAILER_FROM_MOVIE_DIR = MoviesContract.TrailerEntry.buildTrailersUri(60);
    private static final Uri TEST_REVIEW_FROM_MOVIE_DIR = MoviesContract.ReviewEntry.buildReviewsUri(60);

    public void testUriMatcher() {
        UriMatcher testMatcher = MoviesProvider.buildUriMatcher();

        assertEquals("Error: The Movie URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MoviesProvider.MOVIE);

        assertEquals("Error: The TRAILER URI was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_DIR), MoviesProvider.TRAILERS);

        assertEquals("Error: The REVIEW URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_DIR), MoviesProvider.REVIES);

        assertEquals("Error: The TRAILER FROM MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_FROM_MOVIE_DIR), MoviesProvider.TRAILERS_FROM_MOVIE);
        assertEquals("Error: The REVIEWS FROM MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_FROM_MOVIE_DIR), MoviesProvider.REVIEWS_FROM_MOVIE);
    }
}
