package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.unipi.diplomaThesis.rideshare.R;


public class CustomCardView {
    private Activity c;
    private final int MIN_DISTANCE=40;
    public CustomCardView(Activity c){
        this.c=c;
    }

    /**
     * Create a CardView of a dynamically count Images
     * @param count
     * @param image
     * @return
     */
    public CardView getCardView(int count,Bitmap image){
        CardView cardView = new CardView(c);
        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        cardView.setCardElevation(0);
        cardView.setLayoutParams(new FrameLayout.LayoutParams(60,60));
        FrameLayout.LayoutParams m = (FrameLayout.LayoutParams) cardView.getLayoutParams();
        m.setMarginStart(MIN_DISTANCE*count);
        cardView.setLayoutParams(m);
        cardView.setTranslationZ(-count*30);
        cardView.setRadius(250.f);

        ImageView imageView = new ImageView(c);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(60,60));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (image!=null){
            imageView.setImageBitmap(image);
        }else{
            imageView.setBackgroundResource(R.mipmap.ic_profile_default_foreground);
        }
        cardView.addView(imageView);

        return cardView;
    }
}
