package com.archsorceress.cardkeeper.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by burcuarabaci on 17/11/16.
 */

public class CardItem extends RealmObject{
    @PrimaryKey
    private String id;
    private String title;
    private String detail;
    private String imageUrl;
    private int backgroundColor;
    private int textColor;

    public CardItem(){

    }

    public CardItem create(String id, String title,String detail, String imageUrl, int backgroundColor, int textColor){
        CardItem cardItem = new CardItem();
        cardItem.id = id;
        cardItem.title = title.trim();
        cardItem.detail = detail.trim();
        cardItem.imageUrl = imageUrl;
        cardItem.backgroundColor = backgroundColor;
        cardItem.textColor = textColor;
        return cardItem;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDetail() {
        return detail;
    }
}
