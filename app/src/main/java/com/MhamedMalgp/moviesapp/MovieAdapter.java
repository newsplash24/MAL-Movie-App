package com.MhamedMalgp.moviesapp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import java.util.ArrayList;

public class MovieAdapter extends CursorAdapter {
    private static Context mContext;
    private ArrayList<Movie> movies;
    private Boolean shoudUpdateImages;
    private int moviesNumber;
    private DialogDismisser dialogDismisser;
    private Boolean connectionErrorShowen;
    private Boolean haveInternetConnection;
    private Boolean haveData;
    private Boolean toastShown;

    public MovieAdapter(Context context, Cursor c, int flags, MainActivityFragment MAfragment) {
        super(context, c, flags );

        mContext = context;
        shoudUpdateImages = false;
        moviesNumber = 20;
        connectionErrorShowen = false;
        haveInternetConnection = haveNetworkConnection();
        haveData = false;
        toastShown = false;

        dialogDismisser = (DialogDismisser) MAfragment;
    }
    public interface DialogDismisser {
        void dismissDialog();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        ImageView posterView = new ImageView(mContext);
        posterView.setScaleType(ImageView.ScaleType.FIT_XY);
        posterView.setAdjustViewBounds(true);
        ViewHolder viewHolder = new ViewHolder(posterView);
        posterView.setTag(viewHolder);

        return posterView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if(cursor == null || cursor.getCount() <= 0) {
            Toast.makeText(mContext, "List is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (haveInternetConnection){
            Log.v("Picasso","Online");
            String path = "";
            if(cursor != null ) {
                path = cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH);

            }
            if (view == null) {

                viewHolder.posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                viewHolder.posterView.setAdjustViewBounds(true);
                String url = "http://image.tmdb.org/t/p/w185/" + path;
                loadImageOnline(mContext,viewHolder.posterView,url);

            } else {

                String url = "http://image.tmdb.org/t/p/w185/" + path;
                loadImageOnline(mContext,viewHolder.posterView,url);

            }
        }

        else if(!haveInternetConnection && !haveData) {
            Log.v("Picasso","Offline and "+ haveData);

            if (view == null ) {
                viewHolder.posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                viewHolder.posterView.setAdjustViewBounds(true);

            }
            viewHolder.posterView.setImageResource(R.drawable.thumb);

            if(!connectionErrorShowen){
                showConnectionError();
                connectionErrorShowen = true;
            }
        }


        else {

            Log.v("Picasso","Else");

            String path = "";
            if(cursor != null ) {
                path = cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH);

            }
            else {
                Log.v("Picasso","Image URLLLLLL: "+path);
                return;
            }

            if (view == null) {

                viewHolder.posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                viewHolder.posterView.setAdjustViewBounds(true);
                String url = "http://image.tmdb.org/t/p/w185/" + path;
                loadImage(mContext,viewHolder.posterView,url);

            } else {

                String url = "http://image.tmdb.org/t/p/w185/" + path;
                loadImage(mContext,viewHolder.posterView,url);

            }
        }

    }

    public static class ViewHolder{
        public final ImageView posterView;

        public ViewHolder (ImageView im){
            posterView = im;
        }

    }

    private void loadImage(final Context context,final ImageView img, final String url ){

        Picasso.with(context)
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(img, new Callback() {
                    @Override
                    public void onSuccess() {

                        if(dialogDismisser != null) {
                            dialogDismisser.dismissDialog();

                        }
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(url)
                                .placeholder(R.drawable.thumb)

                                .into(img, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if(dialogDismisser != null) {
                                            dialogDismisser.dismissDialog();

                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                        if(!toastShown)
                                            Toast.makeText(mContext, "Cannot fetch posters. No internet connection!", Toast.LENGTH_LONG).show();
                                        toastShown = true;

                                    }
                                });
                        haveInternetConnection = true;
                    }
                });

    }

    private void loadImageOnline(final Context context,final ImageView img, final String url ){

        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.thumb)
                .into(img, new Callback() {
                    @Override
                    public void onSuccess() {
                        if(dialogDismisser != null) {
                            dialogDismisser.dismissDialog();

                        }
                    }

                    @Override
                    public void onError() {
                        Log.v("Picasso","Could not fetch image");

                        if(!toastShown)
                            Toast.makeText(mContext, "Cannot fetch posters. No internet connection!", Toast.LENGTH_LONG).show();
                        toastShown = true;
                    }
                });

    }


    private void showConnectionError(){


        new AlertDialog.Builder(mContext)
                .setTitle("Cannot Fetch Images")
                .setMessage("No saved images and fetching failed. Please make sure your have a working internet connection and try again.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }


    @Override
    public Cursor swapCursor(Cursor newCursor) {
        connectionErrorShowen = false;
        haveInternetConnection = haveNetworkConnection();
        toastShown = false;
        return super.swapCursor(newCursor);
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

    public void setHasData(Boolean hd){
        haveInternetConnection = haveNetworkConnection();
        haveData = hd;
    }

//    public void setMode(int mode){
//
//    }
//    public void updateFavorites(){
////        mCursor.requery();
//        notifyDataSetChanged();
////        swapCursor(mCursor);
//    }
}
