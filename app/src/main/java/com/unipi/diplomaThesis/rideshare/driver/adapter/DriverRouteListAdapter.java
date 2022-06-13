package com.unipi.diplomaThesis.rideshare.driver.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.CustomCardView;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Routes;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.IOException;
import java.util.List;

public class DriverRouteListAdapter extends RecyclerView.Adapter<DriverRouteListAdapter.ViewHolder> {
    private OnClickDriverRoute onClickDriverRoute;
    private List<Routes> routesList;
    private Activity c;
    Driver driver;

    @NonNull
    @Override
    public DriverRouteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.driver_route_layout, parent, false);
        return new DriverRouteListAdapter.ViewHolder(view);
    }

    public DriverRouteListAdapter(Activity c, List<Routes> routesList, Driver driver, OnClickDriverRoute onClickDriverRoute) {
        this.c = c;
        this.driver = driver;
        this.routesList = routesList;
        this.onClickDriverRoute = onClickDriverRoute;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DriverRouteListAdapter.ViewHolder holder, int position) {
        holder.onClickDriverRoute = this.onClickDriverRoute;
        holder.makeRouteOptionsUnvisible();
//      Current Routes Object
        Routes currentRoutes = routesList.get(position);
//        Start and End Points
//            load addresses from the LatLng for the textViews
        Geocoder g = new Geocoder(c);
        String startingAddress;
        String endAddress;
        try {
            Address a = g.getFromLocation(currentRoutes.getRouteLatLng().getStartLat(), currentRoutes.getRouteLatLng().getStartLng(), 1).get(0);
            startingAddress =c.getString(R.string.from)+" " + a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            a = g.getFromLocation(currentRoutes.getRouteLatLng().getEndLat(), currentRoutes.getRouteLatLng().getEndLng(),1).get(0);
            endAddress =c.getString(R.string.to)+" " + a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            holder.startingRoute.setText(User.reformatLengthString(startingAddress,50));
            holder.endRoute.setText(User.reformatLengthString(endAddress,50));
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.routeName.setText(User.reformatLengthString(currentRoutes.getName(),35));
        holder.costPerPassenger.setText(currentRoutes.getCostPerRider()+" €");

        List<String> passengersId = currentRoutes.getPassengersId();
        CustomCardView customCardView = new CustomCardView(c);
        for (int i =0; i<passengersId.size();i++){
            int finalI = i;
            driver.loadRouteRiderIcon(passengersId.get(i), image -> {
                CardView c =customCardView.getCardView(finalI, image);
                c.bringToFront();
                holder.frameLayoutPassengersIcon.addView(c);
            });
        }
        for (int i = currentRoutes.getPassengersId().size(); i< currentRoutes.getRideCapacity(); i++){
            CardView c =customCardView.getCardView(i, null);
            holder.frameLayoutPassengersIcon.addView(c);
        }
        if (routesList.size() == 0){
            holder.division.setVisibility(View.INVISIBLE);
        }else if (routesList.size() == position){
            holder.division.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return routesList.size();
    }
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView startingRoute;
        private TextView endRoute;
        private TextView costPerPassenger;
        private TextView routeName;
        private FrameLayout frameLayoutPassengersIcon;
        private View division;
        private ImageView showRoute ,deleteRoute, editRoute;
        private OnClickDriverRoute onClickDriverRoute;
        private LinearLayout linearLayoutRouteOptions;
        TableRow tableRowRouteInfo;
        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View v) {
            super(v);
            division = v.findViewById(R.id.view3);
            startingRoute = v.findViewById(R.id.textViewLastMessege);
            endRoute = v.findViewById(R.id.textViewDestinationRoutePoint);
            costPerPassenger = v.findViewById(R.id.textViewRouteCost);
            frameLayoutPassengersIcon = (FrameLayout) v.findViewById(R.id.frameLayoutRidersImages);
            routeName = v.findViewById(R.id.textViewRouteName);
            linearLayoutRouteOptions = v.findViewById(R.id.linearLayoutRouteOptions);
            deleteRoute = v.findViewById(R.id.routeDelete);
            linearLayoutRouteOptions.setVisibility(View.INVISIBLE);
            tableRowRouteInfo = v.findViewById(R.id.test);
            editRoute = v.findViewById(R.id.routeEdit);
            showRoute = v.findViewById(R.id.routeShow);
            makeRouteOptionsUnvisible();
        }
        void makeRouteOptionsUnvisible(){
            showRoute.setVisibility(View.INVISIBLE);
            deleteRoute.setVisibility(View.INVISIBLE);
            editRoute.setVisibility(View.INVISIBLE);
            tableRowRouteInfo.setOnClickListener(this);
            linearLayoutRouteOptions.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    linearLayoutRouteOptions.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    TranslateAnimation animate2 = new TranslateAnimation(
                            0,
                            linearLayoutRouteOptions.getWidth(),
                            0,
                            0
                    );
                    animate2.setDuration(500);
                    animate2.setFillAfter(true);
                    linearLayoutRouteOptions.startAnimation(animate2);
                }
            });

        }
        @Override
        public void onClick(View view) {
            onClickDriverRoute.itemClick(view,linearLayoutRouteOptions,showRoute,editRoute,deleteRoute, getAdapterPosition());
        }
    }
}
