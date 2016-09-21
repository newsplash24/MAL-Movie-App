package com.MhamedMalgp.moviesapp;

import java.io.Serializable;

public class Movie implements Serializable {
    private String posterPath;
    private String title;
    private String overview;
    private String releaseDate;
//    private int _ID;
    private int movieID;
    private double voteAverage;






    Movie(String posterPath, String title, double voteAverage, String overview,
          String releaseDate, int movieID){

        this.posterPath = posterPath;
        this.title = title;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
//        this._ID = _ID;
        this.movieID = movieID;
    }

    public void setVoteAverage(double vote_average) {
        this.voteAverage = vote_average;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterPath(String poster_path) {
        this.posterPath = poster_path;
    }

    public void setReleaseDate(String poster_path) {
        this.posterPath = poster_path;
    }
    public void set_ID(int _ID) {
        setMovieID(_ID);
    }


    public double getVoteAverage() {
        return voteAverage;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return this.releaseDate;
    }
    public int get_ID() {
        return getMovieID();
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getMovieID() {
        return movieID;
    }
}
