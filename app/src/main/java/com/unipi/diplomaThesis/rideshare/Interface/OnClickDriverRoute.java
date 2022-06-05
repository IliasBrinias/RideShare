package com.unipi.diplomaThesis.rideshare.Interface;

import android.view.View;
import android.widget.ImageView;

public interface OnClickDriverRoute {
    void itemClick(View routeInfo, View layoutOptions, ImageView show, ImageView edit, ImageView delete, int position);
}
