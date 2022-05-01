package com.unipi.diplomaThesis.rideshare;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap map;
    double EDGES_OFFSET_FOR_MAP = 0.10;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    public void addSpot(Place p){
        map.addMarker(new MarkerOptions().position(p.getLatLng()).title(p.getAddress()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(p.getLatLng(),12));
    }
    PolylineOptions directions;
    public void drawDirection(LatLng start, LatLng end,int width,int height,Direction direction){
        if (directions!=null){
            map.clear();
            directions = null;
        }
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(start);
        startMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_finish_route));
//        map.addMarker(new MarkerOptions().position(start));
//        map.addMarker(new MarkerOptions().position(end));
        map.addMarker(startMarker);

        MarkerOptions destinationMarker = new MarkerOptions();
        destinationMarker.position(start);
        destinationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_point));
        map.addMarker(destinationMarker);
        Route r = direction.getRouteList().get(0);
        List<LatLng> paths = r.getLegList().get(0).getDirectionPoint();
        directions = new PolylineOptions();
        for (LatLng path:paths) {
            directions.add(path);
        }
        directions.color(Color.RED);
        directions.width(5);
        map.addPolyline(directions);
        // zoom to the directions
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(start)
                .include(end)
                .build();
        int padding = (int) (width * EDGES_OFFSET_FOR_MAP); // offset from edges of the map 30% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        map.animateCamera(cu);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }
}