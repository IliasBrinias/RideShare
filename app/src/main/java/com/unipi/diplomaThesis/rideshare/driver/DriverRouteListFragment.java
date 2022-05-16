package com.unipi.diplomaThesis.rideshare.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.unipi.diplomaThesis.rideshare.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverRouteListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverRouteListFragment extends Fragment {

    public DriverRouteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_driver_route_list, container, false);
        return v;
    }
}