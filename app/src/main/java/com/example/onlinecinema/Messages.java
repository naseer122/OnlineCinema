package com.example.onlinecinema;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Messages {
    private String text;
    private long timestamp;
    private String userType;
    private String userId;  // Add user ID
    private String userName; // Add user name
    private String city;     // Add user city
    private String mobile;
    private String push;

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }

    public Messages() {
        // Default constructor required for Firebase
    }

    public Messages(String text, long timestamp, String userId, String userName, String city, String mobile,String userType,String push) {
        this.text = text;
        this.timestamp = timestamp;
        this.userType = userType;
        this.userId = userId;
        this.userName = userName;
        this.city = city;
        this.mobile = mobile;
        this.push = push;
    }
    public String getFormattedTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserType() {
        return userType;
    }
}
