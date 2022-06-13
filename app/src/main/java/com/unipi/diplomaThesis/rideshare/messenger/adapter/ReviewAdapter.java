package com.unipi.diplomaThesis.rideshare.messenger.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Model.Reviews;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{
    List<Reviews> reviewsList;

    public ReviewAdapter(List<Reviews> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_layout, parent, false);
        return new ReviewAdapter.ViewHolder(view);
    }
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        holder.reviewDate.setText(simpleDateFormat.format(reviewsList.get(position).getTimestamp()));
        holder.reviewDescription.setText(reviewsList.get(position).getDescription());
        holder.userReview.setText(String.valueOf(reviewsList.get(position).getReview()));
        User.loadUser(reviewsList.get(position).getUserId(), u->{
            holder.username.setText(User.reformatLengthString(u.getFullName(),30));
            makeDataVisible(holder);
        });
    }
    private void makeDataVisible(ReviewAdapter.ViewHolder holder){
        holder.reviewDate.setVisibility(View.VISIBLE);
        holder.reviewDescription.setVisibility(View.VISIBLE);
        holder.userReview.setVisibility(View.VISIBLE);
        holder.username.setVisibility(View.VISIBLE);

    }
    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, userReview, reviewDescription, reviewDate;
        public ViewHolder(@NonNull View v) {
            super(v);
            username = v.findViewById(R.id.textViewUserName);
            username.setVisibility(View.INVISIBLE);
            userReview = v.findViewById(R.id.textViewReviewAverage);
            userReview.setVisibility(View.INVISIBLE);
            reviewDescription = v.findViewById(R.id.textViewUserReviewDescription);
            reviewDescription.setVisibility(View.INVISIBLE);
            reviewDate = v.findViewById(R.id.textViewDate);
            reviewDate.setVisibility(View.INVISIBLE);
        }
    }
}
