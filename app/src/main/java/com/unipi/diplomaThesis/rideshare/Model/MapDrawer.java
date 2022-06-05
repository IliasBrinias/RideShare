package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.unipi.diplomaThesis.rideshare.R;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.util.List;

public class MapDrawer {
    private double edgesOffsetFromTheMap = 0.05;
    private MarkerOptions startMarker;
    private MarkerOptions destinationMarker;
    private GoogleMap map;
    private Context c;
    private PolylineOptions directions=null;
    private double distance;

    public MapDrawer(Context c, GoogleMap map) {
        this.c=c;
        this.map =map;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.9908372,23.7383394),5));
    }

    public double getEdgesOffsetFromTheMap() {
        return edgesOffsetFromTheMap;
    }

    public void setEdgesOffsetFromTheMap(double edgesOffsetFromTheMap) {
        this.edgesOffsetFromTheMap = edgesOffsetFromTheMap;
    }

    public MarkerOptions getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(MarkerOptions startMarker) {
        this.startMarker = startMarker;
    }

    public MarkerOptions getDestinationMarker() {
        return destinationMarker;
    }

    public void setDestinationMarker(MarkerOptions destinationMarker) {
        this.destinationMarker = destinationMarker;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public Context getC() {
        return c;
    }

    public void setC(Context c) {
        this.c = c;
    }

    public PolylineOptions getDirections() {
        return directions;
    }

    public void setDirections(PolylineOptions directions) {
        this.directions = directions;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void directions(String apiKey, LatLng startingPoint, LatLng destinationPoint, View mapViewBounds){
        GoogleDirection.withServerKey(apiKey)
                .from(startingPoint)
                .to(destinationPoint)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(@Nullable Direction direction) {
                        drawDirection(startingPoint,destinationPoint, mapViewBounds.getWidth(), mapViewBounds.getHeight(),direction);
                    }
                    @Override
                    public void onDirectionFailure(@NonNull Throwable t) {
                    }
                });
    }

    private void drawDirection(LatLng start, LatLng end,int width,int height,Direction direction){
        if (directions!=null){
            map.clear();
            directions = null;
        }
//        origin point
        startMarker = new MarkerOptions();
        startMarker.position(start);
        startMarker.icon(bitmapDescriptorFromVector(c, R.drawable.ic_origin_point));
        origin = map.addMarker(startMarker);

//        destination point
        destinationMarker = new MarkerOptions();
        destinationMarker.position(end);
        destinationMarker.icon(bitmapDescriptorFromVector(c,R.drawable.ic_finish_route));
        destination = map.addMarker(destinationMarker);
//        draw the route
        com.akexorcist.googledirection.model.Route r = direction.getRouteList().get(0);
        List<LatLng> paths = r.getLegList().get(0).getDirectionPoint();
        distance = r.getTotalDistance().doubleValue();
        directions = new PolylineOptions();
        for (LatLng path:paths) {
            directions.add(path);
        }
        directions.color(Color.BLACK);
        directions.width(5);
        map.addPolyline(directions);
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public void moveCameraToTop(LatLng start, LatLng end,int width,int height,View bottomSheet){
        int padding = (int) (width * edgesOffsetFromTheMap);
        // zoom to the directions
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(start)
                .include(end)
                .build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        map.animateCamera(cu, 1, new GoogleMap.CancelableCallback() {
            @Override
            public void onCancel() {

            }
            @Override
            public void onFinish() {
                CameraPosition cameraPosition = map.getCameraPosition();
                GlobalCoordinates startCoordinates = new GlobalCoordinates(bounds.getCenter().latitude, bounds.getCenter().longitude);
                GeodeticCalculator geoCalc = new GeodeticCalculator();
                GlobalCoordinates target = geoCalc.calculateEndingGlobalCoordinates(
                        Ellipsoid.WGS84, startCoordinates, cameraPosition.bearing, 150f);
                LatLng mapTarget = new LatLng(target.getLatitude(), target.getLongitude());
                CameraPosition cap = new CameraPosition(mapTarget,
                        cameraPosition.zoom, cameraPosition.tilt, cameraPosition.bearing);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cap));
            }
        });
        map.setPadding(0,0,0,bottomSheet.getHeight());
    }
    Marker origin,destination;
    public void setOriginSpot(LatLng point){
        if (origin!=null){
            origin.remove();
        }
        startMarker = new MarkerOptions();
        startMarker.position(point);
        startMarker.icon(bitmapDescriptorFromVector(c, R.drawable.ic_origin_point));
        origin = map.addMarker(startMarker);
        updateCamera(point);
    }
    public void setDestinationSpot(LatLng point){
        if (destination!=null){
            destination.remove();
        }
        destinationMarker = new MarkerOptions();
        destinationMarker.position(point);
        destinationMarker.icon(bitmapDescriptorFromVector(c, R.drawable.ic_finish_route));
        destination = map.addMarker(destinationMarker);
        updateCamera(point);

    }
    private void updateCamera(LatLng point){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
    }
    public void zoomToDirection(View mapView,LatLng startPoint, LatLng destinationPoint){
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(startPoint)
                .include(destinationPoint)
                .build();
        int padding = (int) (mapView.getWidth() * this.getEdgesOffsetFromTheMap()); // offset from edges of the map - 20% of screen
        System.out.println(mapView.getWidth()+","+mapView.getHeight());
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,mapView.getWidth(),mapView.getHeight(),padding));
    }
}
