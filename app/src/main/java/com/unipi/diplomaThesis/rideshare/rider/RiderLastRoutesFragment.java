package com.unipi.diplomaThesis.rideshare.rider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.unipi.diplomaThesis.rideshare.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RiderLastRoutesFragment extends Fragment {

    public RiderLastRoutesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rider_last_routes, container, false);
        return v;
    }
}