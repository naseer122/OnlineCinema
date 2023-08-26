package com.example.onlinecinema;

public class FilmModel {
    int image;
    String text;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FilmModel(int image, String text) {
        this.image = image;
        this.text = text;
    }
}
