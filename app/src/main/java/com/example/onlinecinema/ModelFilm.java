package com.example.onlinecinema;

public class ModelFilm {
    String extendedduration,filmurl,img,price,totalduration,filmname,status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilmname() {
        return filmname;
    }

    public void setFilmname(String filmname) {
        this.filmname = filmname;
    }

    public String getExtendedduration() {
        return extendedduration;
    }

    public void setExtendedduration(String extendedduration) {
        this.extendedduration = extendedduration;
    }

    public String getFilmurl() {
        return filmurl;
    }

    public void setFilmurl(String filmurl) {
        this.filmurl = filmurl;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotalduration() {
        return totalduration;
    }

    public void setTotalduration(String totalduration) {
        this.totalduration = totalduration;
    }

    public ModelFilm() {
    }
}
