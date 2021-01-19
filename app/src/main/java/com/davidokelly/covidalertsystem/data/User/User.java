package com.davidokelly.covidalertsystem.data.User;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    private Geocoder geocoder;
    private String firstName;
    private String surname;
    private String email;
    private String address;
    private GeoPoint home;

    public User(String streetNum, String streetName, String town, String county, String postcode, String firstName, String surname, String email, Context context,Locale locale) {
        this.geocoder = new Geocoder(context, locale);
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        String d = ", ";
        if (county.isEmpty()) {
            this.address = streetNum + d + streetName + d + town + d + postcode;
        } else {
            this.address = streetNum + d + streetName + d + town + d + county + d + postcode;
        }
        try {
            Address address = this.geocoder.getFromLocationName(this.address, 1).get(0);
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            this.home = new GeoPoint(lat,lng);
        } catch (IOException e) {
            e.printStackTrace();
            this.home = new GeoPoint(0,0);
        }
    }

    public User(String firstName, String surname, String email, double latitude, double longitude, Context context, Locale locale) {
        this.geocoder = new Geocoder(context, locale);
        try {
            Address geoAddress = this.geocoder.getFromLocation(latitude, longitude, 1).get(0);
            this.address = addressToString(geoAddress);
        } catch (IOException e) {
            this.address = "Error Setting Address";
            e.printStackTrace();
        }
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        this.home = new GeoPoint(latitude, longitude);
    }

    public User() {
    }

    public String addressToString(Address address) {
        String d = ", ";
        return address.getSubThoroughfare() +
                d +
                address.getThoroughfare() +
                d +
                address.getSubAdminArea() +
                d +
                address.getAdminArea() +
                d +
                address.getPostalCode();
    }

    public GeoPoint getHome() {
        return home;
    }

    public void setHome(GeoPoint home) {
        this.home = home;
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
