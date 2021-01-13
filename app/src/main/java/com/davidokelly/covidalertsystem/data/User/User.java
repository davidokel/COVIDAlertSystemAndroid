package com.davidokelly.covidalertsystem.data.User;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String streetNum;
    private String streetName;
    private String town;
    private String county;
    private String postcode;
    private String firstName;
    private String surname;
    private String email;
    private String address;
    private GeoPoint home;

    public User(String streetNum, String streetName, String town, String county, String postcode, String firstName, String surname, String email) {
        this.streetNum = streetNum;
        this.streetName = streetName;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        String d = ", ";
        if (county.isEmpty()) {
            this.address = streetNum + d + streetName + d + town + d + postcode;
        } else {
            this.address = streetNum + d + streetName + d + town + d + county + d + postcode;
        }
        //TODO get latitude and longitude from address
    }

    public User(String firstName, String surname, String email, double latitude, double longitude) {
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        this.home = new GeoPoint(latitude, longitude);
        //TODO get address from lat/long
    }


    public GeoPoint getHome() {
        return home;
    }

    public void setHome(GeoPoint home) {
        this.home = home;
    }

    public String getStreetNum() {
        return streetNum;
    }

    public void setStreetNum(String streetNum) {
        this.streetNum = streetNum;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
