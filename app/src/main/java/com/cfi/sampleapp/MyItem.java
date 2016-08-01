

package com.cfi.sampleapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    String cost, space_left, address, city, name, landmark;
    double lat, lng;

    public MyItem(double lat, double lng, String cost, String space_left, String address, String city, String name, String landmark) {
        this.cost = cost;
        this.space_left = space_left;
        this.address = address;
        this.city = city;
        this.name = name;
        this.landmark = landmark;
        mPosition = new LatLng(lat, lng);
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getCost() {
        return cost;
    }

    public String getSpace_left() {
        return space_left;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getLandmark() {
        return landmark;
    }


}
