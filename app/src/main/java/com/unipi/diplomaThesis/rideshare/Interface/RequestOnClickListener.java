package com.unipi.diplomaThesis.rideshare.Interface;

import android.view.View;

public interface RequestOnClickListener {
    void accept(View view, int position);
    void decline(View view, int position);
}
