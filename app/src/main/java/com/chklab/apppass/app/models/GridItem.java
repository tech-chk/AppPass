package com.chklab.apppass.app.models;

import android.graphics.Bitmap;

/**
 * Created by 010144 on 14/03/25.
 */
public class GridItem {
    String imagePath;
    String date;
    String title;
    String sub;

    public GridItem(String imagePath, String date, String title, String sub) {
        super();
        this.imagePath = imagePath;
        this.date = date;
        this.title = title;
        this.sub = sub;
    }
    public String getImage() {
        return imagePath;
    }
    public void setImage(String imagePath) {
        this.imagePath = imagePath;
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
