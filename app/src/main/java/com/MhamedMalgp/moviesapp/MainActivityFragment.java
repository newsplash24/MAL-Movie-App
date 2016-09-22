package com.MhamedMalgp.moviesapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.MhamedMalgp.moviesapp.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks <Cursor> ,
     MovieAdapter.DialogDismisser
{

    public static final String TAG = MainActivityFragment.class.getSimpleName();
    public static final int MODE_OFFLINE = 0;
    public static final int MODE_ONLINE = 1;
    private int mPosition;
    private ArrayList<Movie> mMovies;
    static MovieAdapter mAdapter;
    static GridView gridview;

    private String mCategory;
    static int LOADER_ID=2;
    private Boolean justLuanched = true;
    private Boolean topRatedTaskSucceed = false;
    private Boolean mostPopularTaskSucceed = false;
    private Boolean connectionErrorShowed = false;
    private Boolean connectionRequested = false;
    static Boolean fav_shown;
    private ProgressDialog pDialog;
    private SharedPreferences pref;
    private LinearLayout favHeader;
    private final String SELECTED_KEY="selectedPosition";
    private final String SELECTED_CATEGORY_KEY = "selectedCategory";
    private Cursor mCursor;
    private Context mContext;

    static final int COLUMN_ID = 0;
    static final int COLUMN_MOVIE_ID = 1;
    static final int COLUMN_POSTER_PATH =2;
    static final int COLUMN_FAVORITES_FLAG =3;
    static final int COLUMN_OVERVIEW =4;
    static final int COLUMN_TITLE =5;
    static final int COLUMN_VOTE_AVG =6;
    static final int COLUMN_RELEASE_DATE =7;

    private static final String[] MOVIE_COLUMNS = {

            MoviesContract.MovieEntry.TABLE_NAME+"."+MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_KEY,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVG,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    //----------------Loader CallBacks
    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        String category = getCategory();

        Uri catUri = MoviesContract.MovieEntry.builMovieCategoryUri(category);
        String[] selectionArgs = {category};



        if(fav_shown)
            return new CursorLoader(mContext, MoviesContract.MovieEntry.buildFavoritesUri(),
                    MOVIE_COLUMNS, null, null, null);
        else
        return new CursorLoader(mContext,catUri,
                MOVIE_COLUMNS, null, null, null);


    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {

        if(cursor == null)
            return;
        mCursor = cursor;
        if(loadOrDownload(cursor.getCount()>0?true:false)) {
            mAdapter.swapCursor(cursor);
        }
        else {
            mAdapter.setHasData(false);
        }

        gridview.post(new Runnable() {
            @Override
            public void run() {
                gridview.setSelection(0);
            }
        });

        if(mPosition != GridView.INVALID_POSITION &&
                (mCategory != null &&mCategory.equals(pref.getString(getString(R.string.sort_by_key),"Top Rated")))
                )
        {
            gridview.smoothScrollToPosition(mPosition);
        }

        if(pref.getBoolean("FAV_SHOWN",false))
            favHeader.setVisibility(View.VISIBLE);
        else
            favHeader.setVisibility(View.GONE);

    }


    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

//end of CursorCallBacks


    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
         void onItemSelected(Uri movieUri, int position);
    }


    @Override
    public void dismissDialog() {
        if(pDialog != null)
        pDialog.dismiss();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mContext = getActivity();
        getLoaderManager().initLoader(LOADER_ID, null, this);
        if(MainActivity.mTwoPane && mCursor != null && mCursor.getCount() >0) {
            mCursor.moveToFirst();
            int indx_id = mCursor.getColumnIndex(MoviesContract.MovieEntry._ID);
            ((Callback) mContext).onItemSelected(
                    MoviesContract.MovieEntry.builMovieUri(mCursor.getInt(indx_id)), 0);
            mCursor.moveToPrevious();

        }
        super.onActivityCreated(savedInstanceState);
    }

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_posters, container, false);

        mAdapter = new MovieAdapter(getActivity(), null, 0, this);
        gridview = (GridView) rootView.findViewById(R.id.gridview);
        favHeader = (LinearLayout) rootView.findViewById(R.id.fav_header);
        gridview.setAdapter(mAdapter);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        fav_shown = pref.getBoolean("FAV_SHOWN",false);
         if(!fav_shown)
            favHeader.setVisibility(View.GONE);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                MovieAdapter adapter = (MovieAdapter) adapterView.getAdapter();
                Cursor cursor =  adapter.getCursor();


                int indx_id = cursor.getColumnIndex(MoviesContract.MovieEntry._ID);
                if (cursor != null ) {

                    ((Callback) getActivity()).onItemSelected(
                            MoviesContract.MovieEntry.builMovieUri(cursor.getInt(indx_id)), position);

                    mPosition = position;
                    mCategory = pref.getString(getString(R.string.sort_by_key),"Top Rated");
                }
            }
        });



        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY) && savedInstanceState.containsKey(SELECTED_CATEGORY_KEY))
        {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mCategory = savedInstanceState.getString(SELECTED_CATEGORY_KEY);

        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedSortBy = pref.getString(getActivity().getString(R.string.sort_by_key),
                getActivity().getString(R.string.item_top_rated));

        return rootView;

    }

    @Override
    public void onResume() {

        Boolean isResumed = pref.getBoolean("MAF_STOPED",false);
        pref.edit().putBoolean("MAF_STOPED",true).commit();
        if(isResumed&& !MainActivity.mTwoPane && fav_shown)
            getLoaderManager().restartLoader(LOADER_ID, null, MainActivityFragment.this);

        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        pref.edit().putBoolean("MAF_STOPED",true).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedSortBy = pref.getString(getActivity().getString(R.string.sort_by_key),
                getActivity().getString(R.string.item_top_rated));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                TextView tv = (TextView) view;
                if(tv!= null)
                tv.setTextColor(Color.WHITE);
                String selectedSortBy = (String) parent.getSelectedItem();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String storedSortBy = pref.getString(getActivity().getString(R.string.sort_by_key),
                        selectedSortBy);

                if (!justLuanched){

                    if (selectedSortBy.equals(getActivity().getString(R.string.item_top_rated))) {

//                        Toast.makeText(getActivity(), "Excuting top rated", Toast.LENGTH_SHORT).show();
                        pref.edit().putString(getActivity().getString(R.string.sort_by_key),
                                getActivity().getString(R.string.item_top_rated)).commit();
                        pref.edit().putBoolean("FAV_SHOWN",
                                false).commit();
                        connectionErrorShowed = false;
                        fav_shown = false;
                        getLoaderManager().restartLoader(LOADER_ID, null, MainActivityFragment.this);
                    } else if (selectedSortBy.equals(getActivity().getString(R.string.item_most_popular))) {

//                        Toast.makeText(getActivity(), "Excuting most popular", Toast.LENGTH_SHORT).show();
                        pref.edit().putString(getActivity().getString(R.string.sort_by_key),
                                getActivity().getString(R.string.item_most_popular)).commit();
                        pref.edit().putBoolean("FAV_SHOWN",
                                false).commit();
                        connectionErrorShowed = false;
                        fav_shown = false;
                        getLoaderManager().restartLoader(LOADER_ID, null, MainActivityFragment.this);
                    }

                    else if (selectedSortBy.equals(getActivity().getString(R.string.item_favorites))) {

//                        Toast.makeText(getActivity(), "Excuting favrites", Toast.LENGTH_SHORT).show();
                        pref.edit().putBoolean("FAV_SHOWN",
                                true).commit();
                        fav_shown = true;
                        connectionErrorShowed = false;
                        getLoaderManager().restartLoader(LOADER_ID, null, MainActivityFragment.this);
                    }

            }

                justLuanched = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setAdapter(adapter);
        if (storedSortBy.equals(getActivity().getString(R.string.item_most_popular))) {

            spinner.setSelection(1);

        } else if (storedSortBy.equals(getActivity().getString(R.string.item_top_rated))) {

            spinner.setSelection(0);
        }
        if(fav_shown)
            spinner.setSelection(2);

    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(LOADER_ID);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        getLoaderManager().destroyLoader(LOADER_ID);
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(SELECTED_KEY, mPosition);
        outState.putString(SELECTED_CATEGORY_KEY, mCategory);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateMovies();
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateMovies(){
        if(haveNetworkConnection()) {
            downlaodData();
        }
        else
            showConnectionError();
    }

    private  ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr, String category)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String ID = "id";

        JSONObject movieJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(TMDB_LIST);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

        String cat;
        if(category == null)
            cat = getCategory();
        else
            cat = category;

        for (int i = 0; i < moviesArray.length(); i++) {

            String posterPath;
            String title;
            String overview;
            String releaseDate;
            int id;
            double voteRate;

            JSONObject movieDetails = moviesArray.getJSONObject(i);

            posterPath = movieDetails.getString(TMDB_POSTER_PATH);
            title = movieDetails.getString(TMDB_TITLE);
            voteRate = movieDetails.getDouble(TMDB_VOTE_AVERAGE);
            overview = movieDetails.getString(TMDB_OVERVIEW);
            releaseDate = movieDetails.getString(RELEASE_DATE);
            id = movieDetails.getInt(ID);

            ContentValues movieValues = new ContentValues();

            movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_KEY, id);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG,
                    MoviesContract.MovieEntry.FLAG_FALSE);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, voteRate);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_CATEGORY, cat);

            cVVector.add(movieValues);

            mMovies.add(new Movie(posterPath, title, voteRate, overview, releaseDate, id));

        }


        int inserted = 0;

        // add to database
        if (cVVector.size() > 0) {

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

                    inserted = mContext.getContentResolver().
                            bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);

            Log.d(TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

//            int count = 1;
//            for (ContentValues m : cvArray) {
//                Log.v(TAG, "Movie " + count + " Title: " + m.get(MoviesContract.MovieEntry.COLUMN_TITLE));
//                Log.v(TAG, "Movie " + count + " Overview: " + m.get(MoviesContract.MovieEntry.COLUMN_OVERVIEW));
//                Log.v(TAG, "Movie " + count + " Poster Path: " + m.get(MoviesContract.MovieEntry.COLUMN_POSTER_PATH));
//                Log.v(TAG, "Movie " + count + " Release Date: " + m.get(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE));
//                Log.v(TAG, "Movie " + count + " Category: " + m.get(MoviesContract.MovieEntry.COLUMN_CATEGORY));
//                Log.v(TAG, "Movie " + count + " Favorites: " + m.get(MoviesContract.MovieEntry.COLUMN_FAVORITES_FLAG));
//                count++;
//            }
        }

        return mMovies;

    }
    //-----------------------------------------------------------------------------------------------
    private String getCategory(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String item_top_rated = mContext.getString(R.string.item_top_rated);
        String cat = pref.getString(mContext.getString(R.string.sort_by_key),
                    item_top_rated);
        return cat.equals(item_top_rated)? MoviesContract.MovieEntry.CATEGORY_TOP_RATED:
                MoviesContract.MovieEntry.CATEGORY_POPULAR;
    }
    //-----------------------------------------------------------------------------------------------
    private void downlaodData(){
        mMovies = new ArrayList<>();
        pDialog = new ProgressDialog(mContext);

        final LoaderManager loaderManager = getLoaderManager();

        String tag_string_req= "string_req";
        //It will not work if api_key is not added in Strings resources
        String url= mContext.getString(R.string.top_rated_url)+mContext.getString(R.string.api_key);

        pDialog.setMessage("Loading...");
        if(!pDialog.isShowing())
        pDialog.show();
        pDialog.setCanceledOnTouchOutside(false);
        StringRequest stringObjReq= new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());

                        try {
                            topRatedTaskSucceed = true;
                            getMoviesDataFromJson(response, MoviesContract.MovieEntry.CATEGORY_TOP_RATED);
                            if(mostPopularTaskSucceed && topRatedTaskSucceed) {
                                mostPopularTaskSucceed = false;
                                topRatedTaskSucceed = false;
//                                setAdapter();
                                loaderManager.restartLoader(LOADER_ID, null, MainActivityFragment.this);
//
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: "+ error.getMessage());

            }
        });

        Global.getInstance().addToRequestQueue(stringObjReq, tag_string_req);

        String tag_string_req2= "string_req";
        //It will not work if api_key is not added in Strings resources
        String url2= mContext.getString(R.string.most_popular_url)+mContext.getString(R.string.api_key);

        StringRequest stringObjReq2= new StringRequest(Request.Method.GET,
                url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());

                        try {
                            getMoviesDataFromJson(response, MoviesContract.MovieEntry.CATEGORY_POPULAR);
                            mostPopularTaskSucceed = true;
                            if(mostPopularTaskSucceed && topRatedTaskSucceed) {

                                mostPopularTaskSucceed = false;
                                topRatedTaskSucceed = false;

                                loaderManager.restartLoader(LOADER_ID, null, MainActivityFragment.this);

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
;
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: "+ error.getMessage());

            }
        });

        Global.getInstance().addToRequestQueue(stringObjReq2, tag_string_req2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(pDialog!=null)
        pDialog.dismiss();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void showConnectionError(){


        new AlertDialog.Builder(mContext)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        connectionErrorShowed = true;

    }

    private Boolean loadOrDownload(Boolean hasData){

        if (! hasData && haveNetworkConnection() && !fav_shown) {
            if(!connectionRequested) {
                downlaodData();
                connectionRequested = true;
                return false;
            }
        }
        else if(!hasData && !haveNetworkConnection() && !fav_shown) {
            if(!connectionErrorShowed)
            showConnectionError();

            mAdapter.setHasData(false);
            return  false;
        }
        else if (fav_shown && !hasData) {
            Toast.makeText(mContext, "No saved favorites!", Toast.LENGTH_SHORT).show();
                return true;
        }
        else if(hasData) {
            mAdapter.setHasData(true);

        }
            return true;
    }

}
