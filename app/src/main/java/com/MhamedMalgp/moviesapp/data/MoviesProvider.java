package com.MhamedMalgp.moviesapp.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class MoviesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int TRAILERS = 101;
    static final int REVIES = 102;
    static final int MOVIE_ITEM = 200;
    static final int MOVIE_TRAILER_REVIEW = 250;
    static final int TRAILERS_FROM_MOVIE = 300;
    static final int REVIEWS_FROM_MOVIE = 400;
    static final int MOVIE_CATEGORY = 500;
    static final int MOVIE_FAVORITES = 600;
    static final int MOVIE_UPDATE = 700;


    private static final SQLiteQueryBuilder sMovieWithTrailersAndReviewsQueryBuilder;

    static {
        sMovieWithTrailersAndReviewsQueryBuilder = new SQLiteQueryBuilder();

        sMovieWithTrailersAndReviewsQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.TrailerEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry._ID +
                        " = " + MoviesContract.TrailerEntry.TABLE_NAME +
                        "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " INNER JOIN " + MoviesContract.MovieEntry.TABLE_NAME +
                        " ON " + MoviesContract.MovieEntry.TABLE_NAME + "." +
                        MoviesContract.MovieEntry._ID + " = " + MoviesContract.ReviewEntry.TABLE_NAME + "." +
                        MoviesContract.ReviewEntry.COLUMN_MOVIE_ID
        );
    }

    private static final String sMovieByCategorySelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry.COLUMN_CATEGORY + " = ? ";

    private static final String sTrailerSelection =
            MoviesContract.TrailerEntry.TABLE_NAME +
                    "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieSelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry._ID + " = ? ";

    private static final String sFavoritesSelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG + " = ? ";

    private static final String sReviewSelection =
            MoviesContract.ReviewEntry.TABLE_NAME +
                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieTrailerRieviewSelection =
            MoviesContract.ReviewEntry.TABLE_NAME +
                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? AND " +
                    MoviesContract.TrailerEntry.TABLE_NAME +
                    "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";


    private Cursor getTrailersByMovieId(Uri uri, String[] projection) {
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection = sTrailerSelection;
        selectionArgs = new String[]{movieId};

        Cursor cursor = mOpenHelper.getReadableDatabase().query(MoviesContract.TrailerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    private Cursor getMovieByCategory(Uri uri, String[] projection) {
        String cat = MoviesContract.MovieEntry.getCategoryFromUri(uri);

        String[] selectionArgs;
        String selection = sMovieByCategorySelection;
        selectionArgs = new String[]{cat};

        Cursor cursor = mOpenHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        getContext().getContentResolver().notifyChange(uri, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getReviewsByMovieId(Uri uri, String[] projection) {
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection = sReviewSelection;
        selectionArgs = new String[]{movieId};

        Cursor cursor = mOpenHelper.getReadableDatabase().query(MoviesContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    private Cursor getMovieReviewTrailer(Uri uri, String[] projection) {
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection = sMovieTrailerRieviewSelection;
        selectionArgs = new String[]{movieId, movieId};
        Log.v("entered", "Entered and movie id = " + movieId + "/n URI: " + uri);

        return sMovieWithTrailersAndReviewsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getTrailerMovie(Uri uri, String[] projection) {
        String movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection = sTrailerSelection;
        selectionArgs = new String[]{movieId};

        return sMovieWithTrailersAndReviewsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#", MOVIE_ITEM);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*", MOVIE_CATEGORY);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*/fav", MOVIE_FAVORITES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*/update", MOVIE_UPDATE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#/all", MOVIE_TRAILER_REVIEW);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER, TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW, REVIES);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER + "/*", TRAILERS_FROM_MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW + "/*", REVIEWS_FROM_MOVIE);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case TRAILERS:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case MOVIE_ITEM:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_TRAILER_REVIEW:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIES:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case TRAILERS_FROM_MOVIE:
                return MoviesContract.TrailerEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_FROM_MOVIE:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case MOVIE_CATEGORY:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case MOVIE_FAVORITES:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case MOVIE_UPDATE:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case MOVIE_TRAILER_REVIEW: {
                retCursor = getMovieReviewTrailer(uri, projection);

                break;
            }

            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case MOVIE_ITEM: {
                String movie_id = uri.getLastPathSegment();
                selectionArgs = new String[]{movie_id};

                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        sMovieSelection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TRAILERS: {
                retCursor = null;
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }

            case REVIES: {
                retCursor = null;
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case TRAILERS_FROM_MOVIE: {
                retCursor = getTrailersByMovieId(uri, projection);
                break;
            }
            case REVIEWS_FROM_MOVIE: {
                retCursor = getReviewsByMovieId(uri, projection);

                break;
            }

            case MOVIE_CATEGORY: {
                retCursor = getMovieByCategory(uri, projection);
                break;
            }

            case MOVIE_FAVORITES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        sFavoritesSelection,
                        new String[]{MoviesContract.MovieEntry.FLAG_TRUE + ""},
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MovieEntry.builMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }


            case TRAILERS: {
                long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.TrailerEntry.buildTrailersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case REVIES: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowDeleted;

        if (null == selection) selection = "1";

        switch (match) {

            case MOVIE: {
                rowDeleted = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case TRAILERS: {
                rowDeleted = db.delete(MoviesContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case REVIES: {
                rowDeleted = db.delete(MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (rowDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case MOVIE_ITEM:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case MOVIE_UPDATE:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case TRAILERS:
                rowsUpdated = db.update(MoviesContract.TrailerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case REVIES:
                rowsUpdated = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String table_name;
        switch (match) {
            case MOVIE: {
                table_name = MoviesContract.MovieEntry.TABLE_NAME;
                break;
            }
            case TRAILERS: {
                table_name = MoviesContract.TrailerEntry.TABLE_NAME;
                break;
            }
            case REVIES: {
                table_name = MoviesContract.ReviewEntry.TABLE_NAME;
                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(table_name, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}