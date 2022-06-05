package com.unipi.diplomaThesis.rideshare.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderRouteAdapter extends RecyclerView.Adapter<RiderRouteAdapter.ViewHolder>{
    private List<Route> routeList;
    private List<User> driverList;
    private Map<String,Object> driverReview;
    private long userDateTime;
    private OnRiderRouteClickListener onRiderRouteClickListener;
    private ViewGroup parent;
    private Context c;

    public RiderRouteAdapter(Context c, List<Route> routeList,
                             List<User> driverList,
                             long userDateTime,
                             Map<String,Object> driverReview,
                             OnRiderRouteClickListener onRiderRouteClickListener) {
        this.onRiderRouteClickListener = onRiderRouteClickListener;
        this.routeList = routeList;
        this.driverList = driverList;
        this.c=c;
        this.userDateTime = userDateTime;
        this.driverReview = driverReview;
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
        try {
            Address a = g.getFromLocation(currentRoute.getRouteLatLng().getStartLat(), currentRoute.getRouteLatLng().getStartLng(), 1).get(0);
            StringBuilder startingAddress = new StringBuilder();
            if (a.getThoroughfare()!=null) startingAddress.append(a.getThoroughfare()+" ");
            if (a.getFeatureName()!=null) startingAddress.append(a.getFeatureName()+" ");
            if (a.getLocality()!=null) startingAddress.append(a.getLocality()+" ");
            if (a.getCountryName()!=null) startingAddress.append(a.getCountryName());

            a = g.getFromLocation(currentRoute.getRouteLatLng().getEndLat(),currentRoute.getRouteLatLng().getEndLng(),1).get(0);
            StringBuilder endAddress = new StringBuilder();
            if (a.getThoroughfare()!=null) endAddress.append(a.getThoroughfare()+" ");
            if (a.getFeatureName()!=null) endAddress.append(a.getFeatureName()+" ");
            if (a.getLocality()!=null) endAddress.append(a.getLocality()+" ");
            if (a.getCountryName()!=null) endAddress.append(a.getCountryName());
            holder.startPointAddress.setText(c.getString(R.string.from)+" "+startingAddress.toString());
            holder.endPointAddress.setText(c.getString(R.string.to)+" "+endAddress.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (userDateTime==0) {
            holder.tableRowTimeDiff.setVisibility(View.GONE);
            return;
        }
        holder.tableRowTimeDiff.setVisibility(View.VISIBLE);
//          Hour Difference
        holder.timeDifference.setText(currentRoute.getTextForTimeDif(c, userDateTime));
        if (driverReview.size()==0){
            holder.reviewCount.setVisibility(View.GONE);
            holder.finalReviews.setVisibility(View.GONE);
        }else {
            holder.reviewCount.setVisibility(View.VISIBLE);
            holder.finalReviews.setVisibility(View.VISIBLE);
            holder.reviewCount.setText(" ("+((Map<String,Object>) driverReview.get(currentRoute.getRouteId())).get("count")+")");
            holder.finalReviews.setText(String.valueOf(((Map<String,Object>) driverReview.get(currentRoute.getRouteId())).get("total")));

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
        holder.driverName.setText(User.reformatLengthString(driver.getFullName(),25));
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userImage;
        TextView driverName,
                startPointAddress,
                endPointAddress,
                finalReviews,
                reviewCount,
                cost,
                timeDifference;
        TableRow tableRowTimeDiff;
        private OnRiderRouteClickListener onRiderRouteClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.CircleImageDriverImage);
            driverName = itemView.findViewById(R.id.textViewUserName);
            startPointAddress = itemView.findViewById(R.id.textViewLastMessege);
            endPointAddress = itemView.findViewById(R.id.textViewDestinationRoutePoint);
            finalReviews = itemView.findViewById(R.id.textViewDriverRating);
            reviewCount = itemView.findViewById(R.id.textViewCountRatings);
            cost = itemView.findViewById(R.id.textViewRouteCost);
            timeDifference = itemView.findViewById(R.id.textViewTimeDifference);
            tableRowTimeDiff = itemView.findViewById(R.id.tableRowTimeDiff);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRiderRouteClickListener.onRouteClick(view, getAdapterPosition());
                }
            });
        }
    }
}
