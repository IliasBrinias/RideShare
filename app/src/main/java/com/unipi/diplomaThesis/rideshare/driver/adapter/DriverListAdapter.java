package com.unipi.diplomaThesis.rideshare.driver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.ViewHolder> {
    private final SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm",Locale.getDefault());
    private final SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private OnClickDriverRoute onClickDriverRoute;
    private List<Route> routeList = new ArrayList<>();
    private Context c;

    @NonNull
    @Override
    public DriverListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.driver_route_main_layout, parent, false);
        return new DriverListAdapter.ViewHolder(view);
    }

    public DriverListAdapter(Context c, List<Route> routeList, OnClickDriverRoute onClickDriverRoute) {
        this.c = c;
        this.routeList = routeList;
        this.onClickDriverRoute = onClickDriverRoute;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DriverListAdapter.ViewHolder holder, int position) {
        holder.onClickDriverRoute = this.onClickDriverRoute;
//        Distinct line between routes
        if (position == 0) {
            if (routeList.size() > 1) {
                holder.tableRowDistinct.setVisibility(View.VISIBLE);
            } else {
                holder.tableRowDistinct.setVisibility(View.GONE);
            }
        } else if (position == routeList.size() - 1){
            holder.tableRowDistinct.setVisibility(View.GONE);
        }else {
            holder.tableRowDistinct.setVisibility(View.VISIBLE);
        }
//      Current Route Object
        Route currentRoute = routeList.get(position);
//        Fill the data for every Route
//        set Start And Destination Points for the mapIntent
        holder.startLatitude = currentRoute.getRouteLatLng().getStartLat();
        holder.startLongitude = currentRoute.getRouteLatLng().getStartLng();
        holder.destinationLatitude = currentRoute.getRouteLatLng().getEndLat();
        holder.destinationLongitude = currentRoute.getRouteLatLng().getEndLng();
//        Start and End Points
//            load addresses from the LatLng for the textViews
        Geocoder g = new Geocoder(c);
        String startingAddress = null;
        String endAddress = null;
        try {
            Address a = g.getFromLocation(currentRoute.getRouteLatLng().getStartLat(), currentRoute.getRouteLatLng().getStartLng(), 1).get(0);
            startingAddress = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            a = g.getFromLocation(currentRoute.getRouteLatLng().getEndLat(),currentRoute.getRouteLatLng().getEndLng(),1).get(0);
            endAddress = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            holder.startingRoute.setText(startingAddress);
            holder.endRoute.setText(endAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Date
        String[] days = c.getResources().getStringArray(R.array.days);
        StringBuilder stringBuilder = new StringBuilder();
//        if the route has a not a one time trim
        if (currentRoute.getRouteDateTime().getTimetable() != 0) {
            stringBuilder.append(c.getResources().getString(R.string.every));
//            if is custom fill the string Builder with the days
            if (currentRoute.getRouteDateTime().getTimetable() == 5) {
                List<String> days_idx = new ArrayList<>(currentRoute.getRouteDateTime().getSelectedDays().values());
                for (int i = 0; i < days_idx.size(); i++) {
                    stringBuilder.append(days[Integer.parseInt(days_idx.get(i))]);
                    if (i != days_idx.size()-1) {
                        stringBuilder.append(", ");
                    } else {
                        stringBuilder.append("\n");
                    }
                }
                stringBuilder.append(c.getResources().getString(R.string.starts_at)+" ");
                stringBuilder.append(dateFormat.format(currentRoute.getRouteDateTime().getStartDateUnix())+"\n");
            } else {
                stringBuilder.append(days[currentRoute.getRouteDateTime().getTimetable()] + "\n");
            }
            stringBuilder.append(c.getResources().getString(R.string.until)+" " + dateFormat.format(currentRoute.getRouteDateTime().getEndDateUnix()));
        }else {
            stringBuilder.append(c.getResources().getString(R.string.one_time_trip_at)+" "+dateFormat.format(currentRoute.getRouteDateTime().getStartDateUnix())+"\n");
        }
        holder.dateRoute.setText(stringBuilder);
        holder.activePassengers.setText(currentRoute.getPassengersId().size()+"/"+currentRoute.getMaxRiders());

//        check if the additional Info is null
        String costPerRider = currentRoute.getCostPerRider()+"$ ";
        if (costPerRider.equals("$ ")){
            holder.descriptionTitle.setVisibility(View.GONE);
        }else {
            holder.descriptionTitle.setVisibility(View.VISIBLE);
            holder.costPerPassenger.setText(costPerRider + c.getResources().getString(R.string.per_customer));
        }
        String description = currentRoute.getDescription();
        if (description == null){
            holder.descriptionTitle.setVisibility(View.GONE);
            holder.scrollView.setVisibility(View.GONE);
        }else {
            holder.descriptionTitle.setVisibility(View.VISIBLE);
            holder.scrollView.setVisibility(View.VISIBLE);
            holder.description.setText(description);
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
        private TextView dateRoute;
        private TextView activePassengers;
        private TextView costPerPassenger;
        private TextView description;
        private TextView descriptionTitle;
        private ImageView imageDeleteRoute;
        private ImageView imageEditRoute;
        private ImageView imageOpenMaps;
        private ScrollView scrollView;
        private TableRow tableRowDistinct;

        private OnClickDriverRoute onClickDriverRoute;
        private double startLatitude;
        private double startLongitude;
        private double destinationLatitude;
        private double destinationLongitude;


        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View v) {
            super(v);
            startingRoute = v.findViewById(R.id.textViewStartingPoint);
            endRoute = v.findViewById(R.id.textViewEndRoute);
            dateRoute = v.findViewById(R.id.textViewDate);
            activePassengers = v.findViewById(R.id.textViewActivePassengers);
            costPerPassenger = v.findViewById(R.id.textViewPricePerCustomer);
            description = v.findViewById(R.id.textViewDescription);
            imageDeleteRoute = v.findViewById(R.id.imageViewDeleteRoute);
            imageEditRoute = v.findViewById(R.id.imageViewEditRoute);
            imageOpenMaps = v.findViewById(R.id.imageViewOpenMaps);
            scrollView = v.findViewById(R.id.scrollView);
            descriptionTitle = v.findViewById(R.id.textViewDescriptionTitle);
            tableRowDistinct = v.findViewById(R.id.tableRowDistinct);
            imageOpenMaps.setOnClickListener(this);
            imageDeleteRoute.setOnClickListener(this);
            imageEditRoute.setOnClickListener(this);

            // Disallow the touch request for parent scroll on touch of child view
            scrollView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // get the text height and check if the scrollview is Scrollable
                    int viewHeight = scrollView.getMeasuredHeight();
                    int contentHeight = scrollView.getChildAt(0).getHeight();
                    scrollView.setOnTouchListener((view, event) -> {
                        try {
                            if(viewHeight - contentHeight < 0) {
//                                if its scrollable, disallow the recyclers scroll
                                view.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                            return false;
                        }catch (Exception e){
                            return false;
                        }
                    });

                }
            });
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.imageViewDeleteRoute:
                    onClickDriverRoute.deleteDriversRoute(view,getAdapterPosition());
                    break;
                case R.id.imageViewEditRoute:
                    onClickDriverRoute.editDriversRoute(view,getAdapterPosition());
                    break;
                case R.id.imageViewOpenMaps:
                    String uri = "http://maps.google.com/maps?" +
                            "saddr="+ startLatitude + "," + startLongitude +
                            "&daddr=" + destinationLatitude + "," + destinationLongitude+
                            "travelmode=driving";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    c.startActivity(intent);
                    break;
            }
        }
    }
}
