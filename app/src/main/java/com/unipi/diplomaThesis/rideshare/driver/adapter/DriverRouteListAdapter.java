package com.unipi.diplomaThesis.rideshare.driver.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.CustomCardView;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.IOException;
import java.util.List;

public class DriverRouteListAdapter extends RecyclerView.Adapter<DriverRouteListAdapter.ViewHolder> {
    private OnClickDriverRoute onClickDriverRoute;
    private List<Route> routeList;
    private Activity c;
    Driver driver;

    @NonNull
    @Override
    public DriverRouteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.driver_route_layout, parent, false);
        return new DriverRouteListAdapter.ViewHolder(view);
    }

    public DriverRouteListAdapter(Activity c, List<Route> routeList, Driver driver, OnClickDriverRoute onClickDriverRoute) {
        this.c = c;
        this.driver = driver;
        this.routeList = routeList;
        this.onClickDriverRoute = onClickDriverRoute;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DriverRouteListAdapter.ViewHolder holder, int position) {
        holder.onClickDriverRoute = this.onClickDriverRoute;
//      Current Route Object
        Route currentRoute = routeList.get(position);
//        Start and End Points
//            load addresses from the LatLng for the textViews
        Geocoder g = new Geocoder(c);
        String startingAddress = null;
        String endAddress = null;
        try {
            Address a = g.getFromLocation(currentRoute.getRouteLatLng().getStartLat(), currentRoute.getRouteLatLng().getStartLng(), 1).get(0);
            startingAddress =c.getString(R.string.from)+" " + a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            a = g.getFromLocation(currentRoute.getRouteLatLng().getEndLat(),currentRoute.getRouteLatLng().getEndLng(),1).get(0);
            endAddress =c.getString(R.string.to)+" " + a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            holder.startingRoute.setText(startingAddress);
            holder.endRoute.setText(endAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.routeName.setText(currentRoute.getName());
        holder.costPerPassenger.setText(currentRoute.getCostPerRider()+" €");

        List<String> passengersId = currentRoute.getPassengersId();
        CustomCardView customCardView = new CustomCardView(c);
        for (int i =0; i<passengersId.size();i++){
            int finalI = i;
            driver.loadRouteRiderIcon(passengersId.get(i), image -> {
                CardView c =customCardView.getCardView(finalI, image);
                c.bringToFront();
                holder.frameLayoutPassengersIcon.addView(c);
            });
        }
        for (int i=currentRoute.getPassengersId().size();i<currentRoute.getRideCapacity();i++){
            CardView c =customCardView.getCardView(i, null);
            holder.frameLayoutPassengersIcon.addView(c);
        }
        if (routeList.size() == 0){
            holder.division.setVisibility(View.INVISIBLE);
        }else if (routeList.size() == position){
            holder.division.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }
    public int getItemViewType(int position) {
        return position;
    }
    public void removeAt(int position) {
        routeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, routeList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView startingRoute;
        private TextView endRoute;
        private TextView costPerPassenger;
        private TextView routeName;
        private FrameLayout frameLayoutPassengersIcon;
        private View division;

        private OnClickDriverRoute onClickDriverRoute;


        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View v) {
            super(v);
            division = v.findViewById(R.id.view3);
            startingRoute = v.findViewById(R.id.textViewLastMessege);
            endRoute = v.findViewById(R.id.textViewDestinationRoutePoint);
            costPerPassenger = v.findViewById(R.id.textViewRouteCost);
            frameLayoutPassengersIcon = (FrameLayout) v.findViewById(R.id.frameLayoutRidersImages);
            routeName = v.findViewById(R.id.textViewRouteName);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickDriverRoute.itemClick(view,getAdapterPosition());
        }
    }
}
