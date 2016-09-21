package com.MhamedMalgp.moviesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback,
        DetailActivityFragment.RecreateLoader {

    static Boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

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
}