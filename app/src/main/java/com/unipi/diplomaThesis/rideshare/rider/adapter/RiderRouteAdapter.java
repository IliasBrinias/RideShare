package com.unipi.diplomaThesis.rideshare.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnRiderRouteClickListener;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.Model.UserRating;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderRouteAdapter extends RecyclerView.Adapter<RiderRouteAdapter.ViewHolder>{
    private List<Route> routeList;
    private List<User> driverList;
    private long userDateTime;
    private OnRiderRouteClickListener onRiderRouteClickListener;
    private ViewGroup parent;
    private Context c;

    public RiderRouteAdapter(Context c, List<Route> routeList, List<User> driverList,long userDateTime, OnRiderRouteClickListener onRiderRouteClickListener) {
        this.onRiderRouteClickListener = onRiderRouteClickListener;
        this.routeList = routeList;
        this.driverList = driverList;
        this.c=c;
        this.userDateTime = userDateTime;
    }

    @NonNull
    @Override
    public RiderRouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rider_route_layout, parent, false);
        this.parent = parent;
        return new RiderRouteAdapter.ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RiderRouteAdapter.ViewHolder holder, int position) {
        holder.onRiderRouteClickListener = this.onRiderRouteClickListener;
        Route currentRoute = routeList.get(position);
        holder.cost.setText( currentRoute.getCostPerRider()+"$");
//        Addresses Names
        Geocoder g = new Geocoder(parent.getContext());
        String startingAddress;
        String endAddress;
        try {
            Address a = g.getFromLocation(currentRoute.getRouteLatLng().getStartLat(), currentRoute.getRouteLatLng().getStartLng(), 1).get(0);

            startingAddress = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            a = g.getFromLocation(currentRoute.getRouteLatLng().getEndLat(),currentRoute.getRouteLatLng().getEndLng(),1).get(0);
            endAddress = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
            holder.startPointAddress.setText(c.getString(R.string.from)+" "+startingAddress);
            holder.endPointAddress.setText(c.getString(R.string.to)+" "+endAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Hour Difference
        holder.timeDifference.setText(currentRoute.getTextForTimeDif(c,userDateTime));

        User currentDriver = null;
        for (User driver:driverList){
            if (currentRoute.getDriverId().equals(driver.getUserId())){
                currentDriver = driver;
                break;
            }
        }
        if (currentDriver == null) {
            FirebaseDatabase.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(currentRoute.getDriverId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            loadDriverData(holder, snapshot.getValue(User.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else {
            loadDriverData(holder, currentDriver);
        }
    }
    public void setDateTime(long dateTime){
        this.userDateTime = dateTime;
    }


    private void loadDriverData(RiderRouteAdapter.ViewHolder holder ,User driver){
        driver.loadUserImage(new OnImageLoad() {
            @Override
            public void loadImageSuccess(Bitmap image) {
                if (image!=null) {
                    holder.userImage.setImageBitmap(image);
                }else{
                    holder.userImage.setBackgroundResource(R.drawable.ic_default_profile);
                }
            }
        });
        holder.driverName.setText(driver.getFullName());
        try {
            holder.reviewCount.setText(driver.getUserRating().size());
            Double finalRate = 0.;
            for (UserRating rating:driver.getUserRating().values()) {
                finalRate+=rating.getFinalRate();
            }
            holder.finalReviews.setText(String.valueOf(finalRate));
        }catch (Exception e){

        }
    }

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

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userImage;
        TextView driverName,
                startPointAddress,
                endPointAddress,
                finalReviews,
                reviewCount,
                cost,
                timeDifference;
        private OnRiderRouteClickListener onRiderRouteClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.CircleImageDriverImage);
            driverName = itemView.findViewById(R.id.textViewDriverName);
            startPointAddress = itemView.findViewById(R.id.textViewOriginRoutePoint);
            endPointAddress = itemView.findViewById(R.id.textViewDestinationRoutePoint);
            finalReviews = itemView.findViewById(R.id.textViewDriverRating);
            reviewCount = itemView.findViewById(R.id.textViewCountRatings);
            cost = itemView.findViewById(R.id.textViewRouteCost);
            timeDifference = itemView.findViewById(R.id.textViewTimeDifference);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRiderRouteClickListener.onRouteClick(view, getAdapterPosition());
                }
            });
        }
    }
}
