package com.unipi.diplomaThesis.rideshare.driver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.RequestOnClickListener;
import com.unipi.diplomaThesis.rideshare.Model.Request;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    List<Request> requestList;
    RequestOnClickListener requestOnClickListener;
    Context c;

    public RequestAdapter(List<Request> requestList, Context c, RequestOnClickListener requestOnClickListener) {
        this.requestList = requestList;
        this.requestOnClickListener = requestOnClickListener;
        this.c = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.requests_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.requestOnClickListener = requestOnClickListener;

        Route.loadRoute(requestList.get(position).getRouteId(),route -> {
            holder.routeName.setText(route.getName());
            holder.routeName.setVisibility(View.VISIBLE);
        });

        User.loadUser(requestList.get(position).getRiderId(),u -> {
            holder.userName.setText(User.reformatLengthString(u.getFullName(),20));
            holder.userName.setVisibility(View.VISIBLE);
            holder.userDescription.setText(requestList.get(position).getDescription());
            holder.userDescription.setVisibility(View.VISIBLE);
            double distanceDeviation = requestList.get(position).getDistanceDeviation();
            if (distanceDeviation>1000){
                holder.distanceDeviation.setText(Math.round(distanceDeviation / 1000.) +" km");
            }else {
                holder.distanceDeviation.setText(Math.round(distanceDeviation) +" m");
            }
            holder.distanceDeviation.setVisibility(View.VISIBLE);

            u.loadUserImage(image -> {
                holder.userImage.setBackgroundResource(0);
                holder.userImage.setImageBitmap(null);
                if (image == null){
                    holder.userImage.setBackgroundResource(R.drawable.ic_default_profile);
                }
                holder.userImage.setImageBitmap(image);
                holder.userImage.setVisibility(View.VISIBLE);
            });

        });

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage;
        TextView userName,
                distanceDeviation,
                routeName,
                userDescription;
        Button buttonDecline, buttonAccept;
        RequestOnClickListener requestOnClickListener;
        public ViewHolder(@NonNull View v) {
            super(v);
            userImage = v.findViewById(R.id.imageViewUserImage);
            userName = v.findViewById(R.id.textViewUserName);
            distanceDeviation = v.findViewById(R.id.textViewDistanceDeviation);
            routeName = v.findViewById(R.id.routeName);
            userDescription = v.findViewById(R.id.textViewRequestDescription);
            buttonDecline = v.findViewById(R.id.buttonDecline);
            buttonAccept = v.findViewById(R.id.buttonAccept);

            userImage.setVisibility(View.INVISIBLE);
            userName.setVisibility(View.INVISIBLE);
            distanceDeviation.setVisibility(View.INVISIBLE);
            routeName.setVisibility(View.INVISIBLE);
            userDescription.setVisibility(View.INVISIBLE);

            buttonDecline.setOnClickListener(this);
            buttonAccept.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == buttonAccept.getId()){
                requestOnClickListener.accept(view,getAdapterPosition());
            }else if (view.getId() == buttonDecline.getId())  {
                requestOnClickListener.decline(view,getAdapterPosition());
            }
        }
    }
}
