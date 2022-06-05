package com.unipi.diplomaThesis.rideshare.driver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickDeletePassengerListener;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.util.List;

public class EditPassengersAdapter extends RecyclerView.Adapter<EditPassengersAdapter.ViewHolder>{
    Context c;
    List<String> passengersId;
    OnClickDeletePassengerListener onClickDeletePassengerListener;

    public EditPassengersAdapter(Context c, List<String> passengersId, OnClickDeletePassengerListener onClickListener) {
        this.c = c;
        this.passengersId = passengersId;
        this.onClickDeletePassengerListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_passengers_route_layout, parent, false);
        return new EditPassengersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onClickDeletePassengerListener = onClickDeletePassengerListener;
        User.loadUser(passengersId.get(position),u -> {
            holder.userName.setText(u.getFullName());
            u.loadUserImage(image -> {
                holder.userImage.setImageBitmap(null);
                holder.userImage.setBackgroundResource(0);
                if (image!=null){
                    holder.userImage.setImageBitmap(image);
                }else {
                    holder.userImage.setBackgroundResource(R.drawable.ic_default_profile);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return passengersId.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage;
        TextView userName;
        ImageButton delete;
        OnClickDeletePassengerListener onClickDeletePassengerListener;
        public ViewHolder(@NonNull View v) {
            super(v);
            userImage = v.findViewById(R.id.imageViewProfile);
            userName = v.findViewById(R.id.textViewUserName);
            delete = v.findViewById(R.id.imageButtonDelete);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickDeletePassengerListener.deletePassenger(view,getAdapterPosition());
        }
    }
}
