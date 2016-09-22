package com.MhamedMalgp.moviesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback,
        DetailActivityFragment.RecreateLoader {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    static Boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (findViewById(R.id.details_fragment) != null) {

            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.details_fragment, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTwoPane) {
            getMenuInflater().inflate(R.menu.menu_double, menu);
            item = menu.findItem(R.id.menu_item_share);
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onItemSelected(Uri contentUri, int position) {

        if (mTwoPane) {

            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);
            args.putInt("MOVIE_POSITION", position);
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {

            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    @Override
    public void recreateLoader() {
        MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        maf.getLoaderManager().restartLoader(MainActivityFragment.LOADER_ID, null, maf);
    }

    @Override
    public void recreateShareProvider() {
        DetailActivityFragment daf = (DetailActivityFragment)
                getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);

        daf.mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (daf.mUri != null) {
            Intent i = daf.createShareMovieIntent();
            if (i == null)
                return;
            daf.mShareActionProvider.setShareIntent(i);
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null or no trailers");
        }
    }

    @Override
    protected void onDestroy() {
        MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        maf.getLoaderManager().destroyLoader(maf.LOADER_ID);
        super.onDestroy();
    }
}
