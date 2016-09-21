package com.MhamedMalgp.moviesapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.malgp.moviesapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_TRAILER = "trailer";

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        public static final String COLUMN_PUBLISHER_NAME = "publisher_name";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_MOVIE_KEY = "movie_key";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewsUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

    }


    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_MOVIE_KEY = "movie_key";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static Uri buildTrailersUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

    }

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";


        public static final String COLUMN_MOVIE_KEY = "movie_key";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_FAVORITES_FLAG = "favorites_flag";

        public static final int FLAG_TRUE = 1;
        public static final int FLAG_FALSE = 0;

        public static final String CATEGORY_TOP_RATED = "top_rated";
        public static final String CATEGORY_POPULAR = "popular";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;


        public static Uri builMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri builMovieCategoryUri(String cat) {
            return CONTENT_URI.buildUpon().appendPath(cat)
                    .build();
        }

        public static Uri buildMovieTRAILER(String trailerID) {
            return CONTENT_URI.buildUpon().appendPath(trailerID)
                    .build();
        }

        public static Uri buildMovieTrailerReview(String movieID) {
            return CONTENT_URI.buildUpon().appendPath(movieID).appendPath("all")
                    .build();
        }

        public static Uri buildFavoritesUri() {
            return CONTENT_URI.buildUpon().appendPath("all").appendPath("fav")
                    .build();
        }

        public static Uri buildFavoriteUpdateUri() {
            return CONTENT_URI.buildUpon().appendPath("fav").appendPath("update")
                    .build();
        }

        public static final String sMovieByCategorySelection =
                MoviesContract.MovieEntry.TABLE_NAME +
                        "." + MoviesContract.MovieEntry.COLUMN_CATEGORY + " = ? ";

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getCategoryFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
