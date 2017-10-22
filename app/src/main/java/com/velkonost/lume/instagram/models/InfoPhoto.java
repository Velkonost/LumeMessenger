package com.velkonost.lume.instagram.models;

/**
 * Created by admin on 22.10.2017.
 */

public class InfoPhoto {
    String login, link;
    int likes;
    long date;

    public void InfoPhoto(String login, long date, String link, Integer likes){
        this.login = login;
        this.date = date;
        this.link = link;
        this.likes = likes;
    }

    public String getLogin(){
        return login;
    }

    public long getDate(){
        return date;
    }

    public String getLink(){
        return link;
    }

    public int getLikes(){
        return likes;
    }

    public void setLogin(String login){
        this.login = login;
    }
    public void setLink(String link){
        this.link = link;
    }
    public void setLikes(Integer likes){
        this.likes = likes;
    }
    public void setDate(long date){
        this.date = date;
    }
}
