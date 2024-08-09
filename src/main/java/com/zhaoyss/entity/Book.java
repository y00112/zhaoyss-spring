package com.zhaoyss.entity;


import com.zhaoyss.annotation.Component;

@Component
public class Book extends AbstractEntity{

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
