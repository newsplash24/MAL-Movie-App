package com.MhamedMalgp.moviesapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class DummyAdapter extends BaseAdapter {
    Context mContext;
    public DummyAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public int getCount() {
        return 20;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

        public View getView(int position, View convertView, ViewGroup parent) {


            ViewHolder viewHolder;


            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                ImageView imageView;
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
                viewHolder = new ViewHolder(imageView);
                convertView = imageView;
                convertView.setTag(viewHolder);
               // imageView.setPadding(1, 1, 1, 1);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" +"/is6QqgiPQlI3Wmk0bovqUFKM56B.jpg").networkPolicy(NetworkPolicy.OFFLINE).into(imageView);
            viewHolder.poster.setImageResource(R.drawable.thumb);
            return convertView;
        }

    static class ViewHolder {
        final ImageView poster;
        ViewHolder (ImageView iv){
            poster = iv;
        }

    }

    }

