package com.MhamedMalgp.moviesapp.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }


    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.TrailerEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.ReviewEntry.TABLE_NAME);

        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());


        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> moviesColumnHashSet = new HashSet<String>();
        moviesColumnHashSet.add(MoviesContract.MovieEntry._ID);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_TITLE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POSTER_PATH);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_AVG);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        moviesColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            moviesColumnHashSet.remove(columnName);
        } while(c.moveToNext());


        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                moviesColumnHashSet.isEmpty());
        db.close();
    }

    public void testMovieTable() {

        SQLiteDatabase db = new MoviesDbHelper(mContext).getWritableDatabase();

        ContentValues movieValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, movieValues);

        Cursor movieCursor = db.query(
                MoviesContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        long trailerRowId = insertTrailer(movieRowId);
        long reviewRowId = insertReview(movieRowId);

        assertFalse("Error: Insert Movie Fails", movieRowId==-1);
        assertFalse("Error: Insert Trailer Fails", trailerRowId==-1);
        assertFalse("Error: Insert Review Fails", reviewRowId==-1);
        assertTrue("Error: Select Fails", movieCursor.moveToNext());
        TestUtilities.validateCurrentRecord("testInsertReadDb movieEntry failed to validate",
                movieCursor, movieValues);

        movieCursor.close();
        db.close();
    }


    public long insertTrailer(long movie_id) {

                      MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues testValues = TestUtilities.createTrailerValues(movie_id);

        long trailerRowId;
        trailerRowId = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, testValues);

        assertTrue(trailerRowId != -1);

        Cursor cursor = db.query(
                MoviesContract.TrailerEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        cursor.close();
        db.close();
        return trailerRowId;
    }

    public long insertReview(long movie_id) {

        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createReviewValues(movie_id);

        long reviewRowId;
        reviewRowId = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, testValues);

        assertTrue(reviewRowId != -1);

        Cursor cursor = db.query(
                MoviesContract.ReviewEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        cursor.close();
        db.close();
        return reviewRowId;
    }

}
