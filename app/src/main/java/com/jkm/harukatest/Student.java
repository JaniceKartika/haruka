package com.jkm.harukatest;

public class Student {
    int _id;
    String _name;
    String _latitude;
    String _longitude;

    public Student() {

    }

    public Student(int id, String name, String latitude, String longitude) {
        this._id = id;
        this._name = name;
        this._latitude = latitude;
        this._longitude = longitude;
    }

    public Student(String name, String latitude, String longitude) {
        this._name = name;
        this._latitude = latitude;
        this._longitude = longitude;
    }

    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getLatitude() {
        return this._latitude;
    }

    public void setLatitude(String latitude) {
        this._latitude = latitude;
    }

    public String getLongitude() {
        return this._longitude;
    }

    public void setLongitude(String longitude) {
        this._longitude = longitude;
    }
}