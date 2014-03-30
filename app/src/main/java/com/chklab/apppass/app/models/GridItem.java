package com.chklab.apppass.app.models;

import android.graphics.Bitmap;

/**
 * Created by 010144 on 14/03/25.
 */
public class GridItem {
    Bitmap image;
    String date;
    String title;
    String sub;

    public GridItem(Bitmap image, String date, String title, String sub) {
        super();
        this.image = image;
        this.date = date;
        this.title = title;
        this.sub = sub;
    }
    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getSub(){
        return sub;
    }
    public void setSub(String sub){
        this.sub = sub;
    }
}
