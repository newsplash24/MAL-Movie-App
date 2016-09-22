package com.MhamedMalgp.moviesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle arguments = new Bundle();
        arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());

        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(arguments);
        fragment.setHasOptionsMenu(true);

        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail, fragment).commit();
        }

    }

}
