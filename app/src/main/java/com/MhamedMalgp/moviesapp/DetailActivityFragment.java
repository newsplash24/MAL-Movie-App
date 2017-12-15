package com.MhamedMalgp.moviesapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.MhamedMalgp.moviesapp.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class DetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private TextView titleTv;
    private TextView releaseDateTv;
    private TextView averageRatingTv;
    private TextView descriptionTv;
    private ImageView posterImg;
    private TableLayout trailersTable, reviewsTable;
    private LayoutInflater inflater;
    static final String DETAIL_URI = "URI";
    static Uri mUri;
    private Button trailersButton, reviewsButton;
    private Button favButton;
    private static Boolean TRAILER_TABLE_EXPANDED, REVIEWS_TABLE_EXPANDED;
    private int reviewsTotalResults;
    private int mMovieID;
    private final int TRAILERS_DIALOG = 0;
    private final int REVIEWS_DIALOG = 1;
    private final int FETCHING_REVIEWS_DIALOG = 2;
    private Boolean savedInstanceNull;
    private Boolean manuallyUpdated;
    private Boolean connectionErrorShown;
    private Boolean noTrailers;
    private Boolean REVIEWS_TASK_FINISHED;
    private Boolean Trailers_TASK_FINISHED;
    int[] scrollingPosition;
    private final String SCROLLING_POSITION_KEY = "selectedCategory";
    private Parcelable state;
    private SharedPreferences pref;
    private Cursor mCursor;
    private ScrollView sv;
    static ShareActionProvider mShareActionProvider;
    private ProgressDialog pDialogFetchingReviews, pDialogTrailers,  pDialogReviews;


    private int DETAIL_LOADER = 0;


    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_KEY = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_VOTE_AVG = 4;
    static final int COL_MOVIE_RELEASE_DATE = 5;
    static final int COL_MOVIE_POSTER_PATH = 6;
    static final int COL_MOVIE_FAVORITES_FLAG = 7;


    private static final String[] DETAIL_COLUMNS = {

            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_KEY,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVG,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG,

    };

    public interface RecreateLoader {
        void recreateLoader();
        void recreateShareProvider();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            mCursor = data;
            mMovieID = data.getInt(COL_MOVIE_ID);
            titleTv.setText(data.getString(COL_MOVIE_TITLE));
            averageRatingTv.setText("Rating: " + data.getFloat(COL_MOVIE_VOTE_AVG) + "/10");
            descriptionTv.setText(data.getString(COL_MOVIE_OVERVIEW));
            releaseDateTv.setText(data.getString(COL_MOVIE_RELEASE_DATE));

                Picasso.with(getActivity())
                        .load("http://image.tmdb.org/t/p/w185/" + data.getString(COL_MOVIE_POSTER_PATH)).into(posterImg);

            Boolean favSet = checkFavoritesFlag(data);
            if (!favSet) {
                favButton.setBackgroundDrawable(getActivity().getResources()
                        .getDrawable(R.drawable.button_fav));

            } else {
                favButton.setBackgroundDrawable(getActivity().getResources()
                        .getDrawable(R.drawable.button_fav_selected));

            }

            if (savedInstanceNull && !manuallyUpdated) {
                addTrailerRequest(data.getInt(COL_MOVIE_ID), data.getInt(COL_MOVIE_KEY), 0);
                addReviewRequest(data.getInt(COL_MOVIE_ID), data.getInt(COL_MOVIE_KEY), 1);
            }

            sv.post(new Runnable() {
                @Override
                public void run() {
                    if ( scrollingPosition != null )
                        sv.scrollTo(scrollingPosition[0], scrollingPosition[1]);
                }
            });

        }

    }

    private Boolean checkFavoritesFlag(Cursor cursor) {

        int favFlag = cursor.getInt(cursor.getColumnIndex
                (MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG));

        switch (favFlag) {

            case MoviesContract.MovieEntry.FLAG_TRUE:
                return true;

            case MoviesContract.MovieEntry.FLAG_FALSE:
                return false;
            default:
                Toast.makeText(getActivity(), "Cannot update favorites!", Toast.LENGTH_SHORT).show();
                return false;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public DetailActivityFragment() {
//        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        titleTv = (TextView) root.findViewById(R.id.titleTxt);
        releaseDateTv = (TextView) root.findViewById(R.id.releaseDateTxt);
        averageRatingTv = (TextView) root.findViewById(R.id.voteAverageTxt);
        descriptionTv = (TextView) root.findViewById(R.id.descriptionTxt);
        posterImg = (ImageView) root.findViewById(R.id.poster);
        trailersTable = (TableLayout) root.findViewById(R.id.trailersTable);
        trailersButton = (Button) root.findViewById(R.id.trailers);
        reviewsButton = (Button) root.findViewById(R.id.reviews);
        reviewsTable = (TableLayout) root.findViewById(R.id.reviewsTable);
        favButton = (Button) root.findViewById(R.id.favButton);
        sv = (ScrollView) root.findViewById(R.id.scrollView);
        manuallyUpdated = false;
        connectionErrorShown = false;
        REVIEWS_TASK_FINISHED = false;
        Trailers_TASK_FINISHED = false;
        noTrailers = false;
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        reviewsTotalResults = -1;


        if (savedInstanceState == null) {
            savedInstanceNull = true;
        } else {
            savedInstanceNull = false;
        }

        return root;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        pDialogTrailers = new ProgressDialog(getActivity());
        pDialogFetchingReviews = new ProgressDialog(getActivity());
        pDialogReviews= new ProgressDialog(getActivity());

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        if ( savedInstanceState != null) {
            scrollingPosition = savedInstanceState.getIntArray(SCROLLING_POSITION_KEY);
        }


        trailersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    showErrorDialog("", "No movie selected! Please select a movie.");
                    return;
                }

                pDialogTrailers.setMessage("Loading Trailers...");
                if (trailersTable.getChildCount() == 0) {
                    if(!pDialogTrailers.isShowing())
                    pDialogTrailers.show();
                    final Cursor cursor = getActivity().getContentResolver().query(MoviesContract.TrailerEntry.
                            buildTrailersUri(Long.parseLong(
                                    mUri.getLastPathSegment())), null, null, null, null
                    );
                    if(noTrailers && Trailers_TASK_FINISHED)
                    {
                        Toast.makeText(getActivity(), "No trailers for this movie.", Toast.LENGTH_SHORT).show();
                    }
                    else if (cursor.getCount() <= 0) {
                        showErrorDialog("No Saved Trailers Data", "Please connect to the internet and try again.");
                        connectionErrorShown = true;
                    }

                    int tNumber = 1;
                    final int INDX_KEY = cursor.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY);
                    while (cursor.moveToNext()) {
                        View view = inflater.inflate(R.layout.trailers_table_row, null);
                        TableRow tr = (TableRow) view.findViewById(R.id.row);

                        ((TextView) tr.getChildAt(1)).setText("Trailer " + tNumber++);
                        final String key = cursor.getString(INDX_KEY);
                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" +
                                        key)));
                            }
                        });
                        focusOnView(sv, trailersButton);
                        trailersTable.addView(tr);

                    }
                    pDialogTrailers.dismiss();
                    trailersTable.setVisibility(View.VISIBLE);
                    TRAILER_TABLE_EXPANDED = true;
                    if(cursor != null)
                    cursor.close();
                } else {
                    if (TRAILER_TABLE_EXPANDED) {
                        trailersTable.setVisibility(View.GONE);
                        TRAILER_TABLE_EXPANDED = false;
                    } else {
                        trailersTable.setVisibility(View.VISIBLE);
                        TRAILER_TABLE_EXPANDED = true;
                    }
                }
            }
        });

        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    showErrorDialog("", "No movie selected! Please select a movie.");
                    return;
                }

                pDialogReviews.setMessage("Loading Reviews...");
                pDialogReviews.setCanceledOnTouchOutside(false);
                if (reviewsTable.getChildCount() == 0) {


                    final Cursor cursor = getActivity().getContentResolver().query(MoviesContract.ReviewEntry.
                            buildReviewsUri(Long.parseLong(
                                    mUri.getLastPathSegment())), null, null, null, null
                    );
                    int storedReviews = 0;
                    if (cursor != null)
                        storedReviews = cursor.getCount();
                    if (reviewsTotalResults <= 0 && REVIEWS_TASK_FINISHED == true)
                        Toast.makeText(getActivity(), "No reviews for this movie.", Toast.LENGTH_SHORT).show();
                    else if(storedReviews == 0)
                        Toast.makeText(getActivity(), "No Saved Reviews Data, Please connect to the internet and try again.", Toast.LENGTH_LONG).show();

                    final int INDX_AUTHOR = cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_PUBLISHER_NAME);
                    final int INDX_CONTENT = cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT);
                    final int INDX_URL = cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_URL);
                    if(!pDialogReviews.isShowing())
                    pDialogReviews.show();
                    while (cursor.moveToNext()) {
                        View view = inflater.inflate(R.layout.reviews_table_row, null);
                        TableRow tr = (TableRow) view.findViewById(R.id.row);
                        TextView authorTv = (TextView) tr.findViewById(R.id.author);
                        TextView contentTv = (TextView) tr.findViewById(R.id.content);

                        final String author = cursor.getString(INDX_AUTHOR);
                        final String content = cursor.getString(INDX_CONTENT);
                        final String url = cursor.getString(INDX_URL);

                        authorTv.setText(author);
                        contentTv.setText(content);

                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            }
                        });
                        focusOnView(sv, reviewsButton);
                        reviewsTable.addView(tr);

                    }
                    dismissProgressDialog(REVIEWS_DIALOG);
                    reviewsTable.setVisibility(View.VISIBLE);
                    REVIEWS_TABLE_EXPANDED = true;
                    if(cursor !=null)
                        cursor.close();
                } else {
                    if (REVIEWS_TABLE_EXPANDED) {
                        reviewsTable.setVisibility(View.GONE);
                        REVIEWS_TABLE_EXPANDED = false;
                    } else {
                        reviewsTable.setVisibility(View.VISIBLE);
                        REVIEWS_TABLE_EXPANDED = true;
                    }
                }

            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri == null) {
                    showErrorDialog("", "No movie selected! Please select a movie.");
                    return;
                }
//                Cursor cursor = getActivity().getContentResolver().query(mUri,
//                        null,null,null,null);

                Boolean favSet = null;

                if (mCursor.moveToFirst())
                    favSet = checkFavoritesFlag(mCursor);
                else {
//                    cursor.close();
                    return;
                }

                ContentValues values = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(mCursor, values);
                if (favSet) {
                    values.put(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG,
                            MoviesContract.MovieEntry.FLAG_FALSE
                    );

                    favButton.setBackgroundDrawable(getActivity().getResources()
                            .getDrawable(R.drawable.button_fav));
                    pref.edit().putBoolean("FAV_UNCHECKED", true).commit();

                } else {

                    values.put(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG,
                            MoviesContract.MovieEntry.FLAG_TRUE
                    );
                    pref.edit().putBoolean("FAV_UNCHECKED", false).commit();
                }
                Set set = values.valueSet();
                Iterator iter = set.iterator();
                while (iter.hasNext()) {
                    Log.e("ITER", iter.next().toString());
                }

                try {
                    getActivity().getContentResolver().update(mUri,
                            values, MoviesContract.MovieEntry._ID + "=? ",
                            new String[]{values.get(MoviesContract.MovieEntry._ID) + ""});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                manuallyUpdated = true;
                if (MainActivity.mTwoPane && pref.getBoolean("FAV_SHOWN", false)) {
                    ((RecreateLoader) getActivity()).recreateLoader();
//                    MainActivityFragment.mAdapter.updateFavorites();
                }
            }
        });

        if(savedInstanceState == null && MainActivity.mTwoPane)
        ((RecreateLoader)getActivity()).recreateShareProvider();
        super.onActivityCreated(savedInstanceState);
    }

    private void dismissProgressDialog(int dialog) {
        switch(dialog) {
            case FETCHING_REVIEWS_DIALOG: {
                if (pDialogFetchingReviews != null && pDialogFetchingReviews.isShowing()) {
                    pDialogFetchingReviews.dismiss();
                }
                break;
            }

            case TRAILERS_DIALOG: {
                if (pDialogTrailers != null && pDialogTrailers.isShowing()) {
                    pDialogTrailers.dismiss();
                }
                break;
            }

            case REVIEWS_DIALOG: {
                if (pDialogReviews != null && pDialogReviews.isShowing()) {
                    pDialogReviews.dismiss();
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntArray(SCROLLING_POSITION_KEY, new int[]{ sv.getScrollX(), sv.getScrollY()});
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog(TRAILERS_DIALOG);
        dismissProgressDialog(REVIEWS_DIALOG);
        dismissProgressDialog(FETCHING_REVIEWS_DIALOG);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mUri != null) {
            Intent i = createShareMovieIntent();
            if(i == null)
                return ;
            mShareActionProvider.setShareIntent(i);
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null or no trailers");
        }
    }

    public Intent createShareMovieIntent(){
        Cursor cursor = null;
        try {
            cursor = getActivity().getContentResolver().query(MoviesContract.TrailerEntry.
                    buildTrailersUri(Long.parseLong(
                            mUri.getLastPathSegment())), null, null, null, null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(cursor != null && cursor.moveToFirst()) {
            String key = cursor.getString(cursor.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "This is an awesome movie!" + "  \nhttp://www.youtube.com/watch?v="+key
            );
            cursor.close();
            return  shareIntent;
        }
        //shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        if(cursor !=null)
        cursor.close();

        return null;
    }

    private final void focusOnView(final ScrollView sv, final View v){

        sv.post(new Runnable() {
            @Override
            public void run() {
                if ( scrollingPosition != null )
                sv.scrollTo(scrollingPosition[0], scrollingPosition[1]);
            }
        });
    }

    int getTrailersDataFromJson(String trailersJsonStr, int movieKEY, int movie_ID)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String KEY = "key";
        JSONObject trailerJson = new JSONObject(trailersJsonStr);
        JSONArray trailersArray = trailerJson.getJSONArray(TMDB_LIST);
        if(!trailersArray.toString().contains("key"))
            noTrailers = true;

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

            try {
                getActivity().getContentResolver().
                        bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, cvArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Trailers_TASK_FINISHED = true;
        if(mShareActionProvider != null)
            mShareActionProvider.setShareIntent(createShareMovieIntent());

        return count;
    }

    int getReviewsDataFromJson(String reviewsJsonStr, int movieKEY, int movieID)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String TOTAL_RESULTS = "total_results";
        final String PUBLISHER = "author";
        final String CONTENT = "content";
        final String URL = "url";

        JSONObject reviewJson = new JSONObject(reviewsJsonStr);
        JSONArray reviewsArray = reviewJson.getJSONArray(TMDB_LIST);
        reviewsTotalResults = reviewJson.getInt(TOTAL_RESULTS);
        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewsArray.length());

        for (int i = 0; i < reviewsArray.length(); i++) {

            JSONObject trailerDetails = reviewsArray.getJSONObject(i);
            ContentValues trailerValues = new ContentValues();
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, movieKEY);
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieID);
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_PUBLISHER_NAME,
                    trailerDetails.getString(PUBLISHER));
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT,
                    trailerDetails.getString(CONTENT));
            trailerValues.put(MoviesContract.ReviewEntry.COLUMN_URL, trailerDetails.getString(URL));
            cVVector.add(trailerValues);
        }
        int count = cVVector.size();

        if (count > 0) {

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            try {
                getActivity().getContentResolver().
                        bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        REVIEWS_TASK_FINISHED = true;
        return count;

    }


    void addTrailerRequest(final int movieID, final int movieKEY, int requestNUM) {

        String tag_string_req = "string_req" + (requestNUM);
        //It will not work if api_key is not added in Strings resources
        final String TRAILERS_URL =
                "http://api.themoviedb.org/3/movie/" + movieKEY + "/videos?"
                        + "api_key=" + getString(R.string.api_key);

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
        //It will not work if api_key is not added in Strings resources
        final String TRAILERS_URL =
                "http://api.themoviedb.org/3/movie/" + movieKEY + "/reviews?"
                        + "api_key=" + getString(R.string.api_key);


        pDialogFetchingReviews.setMessage("Fetching Reviews...");
        pDialogFetchingReviews.show();
        pDialogFetchingReviews.setCanceledOnTouchOutside(false);
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
                        dismissProgressDialog(FETCHING_REVIEWS_DIALOG);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("MovieService", "Error: " + error.getMessage());
                if (!haveNetworkConnection()) {
                    showErrorDialog("Cannot fetch latest reviews",
                            "Please make sure you have a working internet connection and reopen this movie.");
                    connectionErrorShown = true;
                }
                reviewsTotalResults = 4;
                dismissProgressDialog(FETCHING_REVIEWS_DIALOG);
            }
        });
        Global.getInstance().addToRequestQueue(stringObjReq, tag_string_req);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void showErrorDialog(String title, String msg) {

        if (!connectionErrorShown)
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
    }

}
