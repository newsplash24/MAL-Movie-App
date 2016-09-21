package com.MhamedMalgp.moviesapp;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.MhamedMalgp.moviesapp.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;


public class MovieService extends IntentService {

    static final String EXTRA_MOVIE_LIST = "com.malgp.moviesapp.extra.MOVIE_LIST";


//    private ArrayList<Movie> mMovies;

    public MovieService() {
        super("FetchTrailersReviewsService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final Cursor cursor = getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, null, null, null, null);
            final int INDX_KEY = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_KEY);
            final int INDX_ID = cursor.getColumnIndex(MoviesContract.MovieEntry._ID);
            int i = 0;

            while (cursor.moveToNext()) {
                addTrailerRequest(cursor.getInt(INDX_ID), cursor.getInt(INDX_KEY), i++);
            }

            cursor.moveToFirst();
            i = 0;

            while (cursor.moveToNext()) {

                final int indx = i;
                addReviewRequest(cursor.getInt(INDX_ID), cursor.getInt(INDX_KEY), i++);

            }
        }
    }

    int getTrailersDataFromJson(String trailersJsonStr, int movieKEY, int movie_ID)
            throws JSONException {


        final String TMDB_LIST = "results";
        final String KEY = "key";

        JSONObject trailerJson = new JSONObject(trailersJsonStr);
        JSONArray trailersArray = trailerJson.getJSONArray(TMDB_LIST);
//        ArrayList<String> trailers = new ArrayList<>();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(trailersArray.length());


        for (int i = 0; i < trailersArray.length(); i++) {

            JSONObject trailerDetails = trailersArray.getJSONObject(i);
            ContentValues trailerValues = new ContentValues();

            trailerValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, movieKEY);
            trailerValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, movie_ID);
            trailerValues.put(MoviesContract.TrailerEntry.COLUMN_KEY, trailerDetails.getString(KEY));
            cVVector.add(trailerValues);
        }
        int count = cVVector.size();

        if (count > 0) {

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            getContentResolver().
                    bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, cvArray);
        }
        return count;

    }

    int getReviewsDataFromJson(String reviewsJsonStr, int movieKEY, int movieID)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String publisher = "author";
        final String content = "content";
        final String url = "url";


        JSONObject reviewJson = new JSONObject(reviewsJsonStr);
        JSONArray reviewsArray = reviewJson.getJSONArray(TMDB_LIST);
//        ArrayList<String> trailers = new ArrayList<>();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewsArray.length());


        for (int i = 0; i < reviewsArray.length(); i++) {

            JSONObject trailerDetails = reviewsArray.getJSONObject(i);
            ContentValues trailerValues = new ContentValues();

            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, movieKEY);
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieID);
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_PUBLISHER_NAME, trailerDetails.getString(publisher));
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, trailerDetails.getString(content));
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_URL, trailerDetails.getString(url));
            cVVector.add(trailerValues);
        }
        int count = cVVector.size();

        if (count > 0) {

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            getContentResolver().
                    bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
        }
        return count;

    }


    void addTrailerRequest(final int movieID, final int movieKEY, int requestNUM) {

        String tag_string_req = "string_req" + (requestNUM);
        final String TRAILERS_URL =
                "http://api.themoviedb.org/3/movie/" + movieKEY + "/videos?"
                        + "api_key=" + getString(R.string.api_key);


//        Log.d("MovieService", cursor.getPosition()+" -- "+cursor.getCount());
        StringRequest stringObjReq = new StringRequest(Request.Method.GET,
                TRAILERS_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("MovieService", response.toString());
                        try {
                            int count = getTrailersDataFromJson(response, movieKEY, movieID);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //pDialog.hide();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("MovieService", "Error: " + error.getMessage());
            }
        });

        Global.getInstance().addToRequestQueue(stringObjReq, tag_string_req);
    }

    void addReviewRequest(final int movieID, final int movieKEY, int requestNUM) {

        String tag_string_req = "string_req" + requestNUM;
        final String TRAILERS_URL =
                "http://api.themoviedb.org/3/movie/" + movieKEY + "/reviews?"
                        + "api_key=" + getString(R.string.api_key);

        StringRequest stringObjReq = new StringRequest(Request.Method.GET,
                TRAILERS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MovieService", response.toString());

                        try {
                            int count = getReviewsDataFromJson(response, movieKEY, movieID);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("MovieService", "Error: " + error.getMessage());
            }
        });

        Global.getInstance().addToRequestQueue(stringObjReq, tag_string_req);
    }
}
