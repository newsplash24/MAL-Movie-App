package com.MhamedMalgp.moviesapp.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.MhamedMalgp.moviesapp.data.MoviesContract.MovieEntry;
import com.MhamedMalgp.moviesapp.data.MoviesContract.ReviewEntry;
import com.MhamedMalgp.moviesapp.data.MoviesContract.TrailerEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                MoviesContract.TrailerEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                MoviesContract.ReviewEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                MoviesContract.TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Trailer table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecordsFromDB() {
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null);
        db.delete(MoviesContract.TrailerEntry.TABLE_NAME, null, null);
        db.delete(MoviesContract.ReviewEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {

            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {

            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(MoviesContract.MovieEntry.CONTENT_URI);

        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MoviesContract.MovieEntry.CONTENT_TYPE, type);
        type = mContext.getContentResolver().getType(MoviesContract.TrailerEntry.CONTENT_URI);
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                MoviesContract.TrailerEntry.CONTENT_TYPE, type);
        type = mContext.getContentResolver().getType(MoviesContract.ReviewEntry.CONTENT_URI);
        assertEquals("Error: the ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_TYPE",
                MoviesContract.ReviewEntry.CONTENT_TYPE, type);


        type = mContext.getContentResolver().getType(
                MoviesContract.MovieEntry.builMovieUri(40));
        assertEquals("Error: the MovieEntry CONTENT_URI with Movie should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);
        type = mContext.getContentResolver().getType(
                MoviesContract.TrailerEntry.buildTrailersUri(40));
        assertEquals("Error: the TrailerEntry CONTENT_URI with Movie should return TrailerEntry.CONTENT_ITEM_TYPE",
                MoviesContract.TrailerEntry.CONTENT_ITEM_TYPE, type);
        type = mContext.getContentResolver().getType(
                MoviesContract.ReviewEntry.buildReviewsUri(40));
        assertEquals("Error: the ReviewEntry CONTENT_URI with Movie should return ReviewEntry.CONTENT_ITEM_TYPE",
                MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicMovieQuery() {

        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Unable to Insert MovieEntry into the Database", movieRowId != -1);

        db.close();

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, testValues);
    }

    public void testBasicTrailerQueries() {

        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);

        ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowId);
        long trailerRowId = db.insert(TrailerEntry.TABLE_NAME, null, trailerValues);

        Cursor trailerCursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicTrailersQueries, Trailers query", trailerCursor, trailerValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    trailerCursor.getNotificationUri(), TrailerEntry.CONTENT_URI);
        }
    }

    public void testBasicReviewQueries() {
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);

        ContentValues reviewValues = TestUtilities.createReviewValues(movieRowId);
        long reviewRowId = db.insert(ReviewEntry.TABLE_NAME, null, reviewValues);

        Cursor reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );


        TestUtilities.validateCursor("testBasicReviewsQueries, Reviews query", reviewCursor, reviewValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    reviewCursor.getNotificationUri(), ReviewEntry.CONTENT_URI);
        }
    }


    public void testUpdateReview() {
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);

        ContentValues values = TestUtilities.createReviewValues(movieRowId);

        Uri reviewUri = mContext.getContentResolver().
                insert(ReviewEntry.CONTENT_URI, values);
        long reviewRowId = ContentUris.parseId(reviewUri);


        assertTrue(reviewRowId != -1);
        Log.d(LOG_TAG, "New row id: " + reviewRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ReviewEntry._ID, reviewRowId);
        updatedValues.put(ReviewEntry.COLUMN_CONTENT, "Maybe i've to watch it again!");

        Cursor reviewCursor = mContext.getContentResolver().query(ReviewEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        reviewCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                ReviewEntry.CONTENT_URI, updatedValues, ReviewEntry._ID + "= ?",
                new String[]{Long.toString(reviewRowId)});
        assertEquals(count, 1);

        tco.waitForNotificationOrFail();

        reviewCursor.unregisterContentObserver(tco);
        reviewCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,   // projection
                ReviewEntry._ID + " =? ",
                new String[]{reviewRowId + ""},   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateReview.  Error validating Review entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createMovieValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);


        ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowId);

        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(TrailerEntry.CONTENT_URI, true, tco);

        Uri trailerInsertUri = mContext.getContentResolver()
                .insert(TrailerEntry.CONTENT_URI, trailerValues);
        assertTrue(trailerInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor trailerCursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );


        ContentValues reviewValues = TestUtilities.createReviewValues(movieRowId);

        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ReviewEntry.CONTENT_URI, true, tco);

        Uri reviewInsertUri = mContext.getContentResolver()
                .insert(ReviewEntry.CONTENT_URI, reviewValues);
        assertTrue(reviewInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrailerEntry insert.",
                trailerCursor, trailerValues);

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ReviewEntry insert.",
                reviewCursor, reviewValues);


        testValues.putAll(trailerValues);
        testValues.putAll(reviewValues);

        cursor = mContext.getContentResolver().query(
                TrailerEntry.buildTrailersUri(movieRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating Trailers Data.",
                cursor, trailerValues);

        // Get the joined Weather and Location data with a start date
        cursor = mContext.getContentResolver().query(
                ReviewEntry.buildReviewsUri(movieRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating Review Data.",
                cursor, reviewValues);
    }


    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        TestUtilities.TestContentObserver trailerObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrailerEntry.CONTENT_URI, true, trailerObserver);

        TestUtilities.TestContentObserver reviewObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrailerEntry.CONTENT_URI, true, reviewObserver);

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        trailerObserver.waitForNotificationOrFail();
        reviewObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
        mContext.getContentResolver().unregisterContentObserver(trailerObserver);
        mContext.getContentResolver().unregisterContentObserver(reviewObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertMovieValues() {

        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieEntry._ID, 46513 + i);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG, MoviesContract.MovieEntry.FLAG_FALSE);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Intersteller");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_KEY, "TEST_KEY" + i);
            movieValues.put(MovieEntry.COLUMN_CATEGORY, MovieEntry.CATEGORY_POPULAR);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "7/8/2016");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, "/982ACS564");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, "8.4");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "Bla bla bla");

            returnContentValues[i] = movieValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {

        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
