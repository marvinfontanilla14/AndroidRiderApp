package com.example.khean07.androidriderapp.Model;

/**
 * Created by Khean07 on 4/14/2018.
 */

public class Data {

    public String title;
    public String detail;

    public Data() {
    }

    public Data(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}