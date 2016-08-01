package com.cfi.sampleapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterItemInfoWindowClickListener<MyItem> {
    GoogleMap googleMap;
    MapFragment mMapFragment;
    private ClusterManager<MyItem> mClusterManager;
    RelativeLayout mInfoCard;
    TextView mName, mAvailability, mAddress, mCost;
    LinearLayout mNavBtn, closeBtn;
    MyItem dMyItem = null;
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mInfoCard = (RelativeLayout) findViewById(R.id.info_card);
        mInfoCard.setVisibility(View.GONE);
        mName = (TextView) findViewById(R.id.name);
        mAvailability = (TextView) findViewById(R.id.availability);
        mAddress = (TextView) findViewById(R.id.address);
        mCost = (TextView) findViewById(R.id.cost);
        mNavBtn = (LinearLayout) findViewById(R.id.navBtn);
        closeBtn = (LinearLayout) findViewById(R.id.closeBtn);

        initilizeMap();

        //launch map to show direction
        mNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = googleMap.getMyLocation();
                try {
                    if (location != null && dMyItem != null) {
                        double pSLatitude = location.getLatitude();
                        double pSLongitude = location.getLongitude();
                        double pELatitude = dMyItem.getLat();
                        double pELongitude = dMyItem.getLng();
                        String query = "http://maps.google.com/maps?saddr=" + pSLatitude + "," + pSLongitude + "&daddr=" + pELatitude + "," + pELongitude;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(query));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfoCard.setVisibility(View.GONE);
            }
        });

    }

    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = mMapFragment.getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "There went something wrong. Please try again later.", Toast.LENGTH_SHORT)
                        .show();
            }

            googleMap.setMyLocationEnabled(true);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.479233398, 77.08119493), 10));

        mClusterManager = new ClusterManager<MyItem>(this, googleMap);
        mClusterManager.setRenderer(new PersonRenderer());
        mClusterManager.setOnClusterItemClickListener(this);
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnCameraChangeListener(mClusterManager);

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
        mClusterManager.cluster();
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.data);
        List<MyItem> items = read(inputStream);
        mClusterManager.addItems(items);
    }

    @Override
    public boolean onClusterItemClick(MyItem myItem) {
        dMyItem = myItem;
        mInfoCard.setVisibility(View.VISIBLE);
        mName.setText(myItem.getName());
        mAvailability.setText(myItem.getSpace_left() + " Parking Available");
        mAddress.setText(myItem.getAddress() + ", " + myItem.getCity());
        mCost.setText(getString(R.string.ruppes_symbol) + myItem.getCost());

        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyItem myItem) {
    }

    private class PersonRenderer extends DefaultClusterRenderer<MyItem> {

        public PersonRenderer() {
            super(getApplicationContext(), googleMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            String title = item.getName();
            markerOptions.title(title);
        }
    }

    public List<MyItem> read(InputStream inputStream) throws JSONException {
        List<MyItem> items = new ArrayList<MyItem>();
        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
        JSONObject jsObj = new JSONObject(json);
        JSONArray array=jsObj.getJSONArray("parking_locations");
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            JSONObject posObj = object.getJSONObject("geo_location");
            double lat = posObj.getDouble("latitude");
            double lng = posObj.getDouble("longitude");
            String space_left = "";
            String cost="";
            JSONArray sub_lotsArr = object.getJSONArray("sub_lots");
            for (int k = 0; k < sub_lotsArr.length(); k++) {
                JSONObject obj = sub_lotsArr.getJSONObject(k);
                space_left = obj.getString("space_left");
                JSONObject parking_chargesObj=obj.getJSONObject("parking_charges");
                cost=parking_chargesObj.getString("cost");
            }

            String landmark = object.getString("landmark");
            String address = object.getString("address");
            String city = object.getString("city");
            String name = object.getString("name");

            items.add(new MyItem(lat, lng, cost, space_left, address, city, name, landmark));
        }
        return items;
    }
}
