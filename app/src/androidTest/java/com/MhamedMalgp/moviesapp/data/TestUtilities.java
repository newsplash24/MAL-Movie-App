package com.MhamedMalgp.moviesapp.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

//import com.malgp.moviesapp.data.PollingCheck;

import com.MhamedMalgp.moviesapp.utils.PollingCheck;

import java.util.Map;
import java.util.Set;


public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.MovieEntry._ID, 46513);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG, MoviesContract.MovieEntry.FLAG_FALSE);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Intersteller");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_CATEGORY, MoviesContract.MovieEntry.CATEGORY_POPULAR);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "7/8/2016");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, "/982ACS564");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, "8.4");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "Bla bla bla");

        return movieValues;
    }

    static ContentValues createTrailerValues(long movie_id) {

        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.TrailerEntry.COLUMN_KEY, "XJ0G5Fpzdf8");
        testValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID,  movie_id);
        testValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY,  "ExAMPLE_KEY");

        return testValues;
    }

    static ContentValues createReviewValues(long movie_id) {

        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.ReviewEntry.COLUMN_PUBLISHER_NAME, "Barak Obama");
        testValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "OOOH! That Movie Was Great!");
        testValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID,  movie_id);
        testValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY,  "ExAMPLE_KEY");
        testValues.put(MoviesContract.ReviewEntry.COLUMN_URL,  "www.example.com");

        return testValues;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {

            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
