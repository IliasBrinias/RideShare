package com.unipi.diplomaThesis.rideshare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnRiderRouteClickListener;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.Route.RouteActivity;
import com.unipi.diplomaThesis.rideshare.rider.adapter.RiderRouteAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RiderLastRoutesFragment extends Fragment {

    RecyclerView recyclerViewActiveRoutes, recyclerViewPreviousRoutes;
    RiderRouteAdapter riderActiveRouteAdapter,riderPreviousRouteAdapter;
    List<Route> activeRoutes = new ArrayList<>();
    List<Route> previousRoutes = new ArrayList<>();
    List<User> routeDrivers= new ArrayList<>();
    User u;
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
        u = User.loadUserInstance(getActivity());
        recyclerViewActiveRoutes=v.findViewById(R.id.recyclerViewActiveRoutes);
        recyclerViewPreviousRoutes=v.findViewById(R.id.recyclerViewPreviousRoutes);
        //        initialize recyclerView
        LinearLayoutManager linearLayoutManagerActive = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager linearLayoutManagerPrevious = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerViewActiveRoutes.setLayoutManager(linearLayoutManagerActive);
        recyclerViewPreviousRoutes.setLayoutManager(linearLayoutManagerPrevious);
        riderActiveRouteAdapter = new RiderRouteAdapter(getActivity(),activeRoutes, routeDrivers, 0,null, new OnRiderRouteClickListener() {
            @Override
            public void onRouteClick(View view, int position) {
                Intent i =new Intent(getActivity(), RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),activeRoutes.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),activeRoutes.get(position).getDriverId());
                startActivity(i);
            }
        });
        riderPreviousRouteAdapter = new RiderRouteAdapter(getActivity(), previousRoutes, routeDrivers, 0,null, (view, position) -> {});
        recyclerViewActiveRoutes.setAdapter(riderActiveRouteAdapter);
        recyclerViewPreviousRoutes.setAdapter(riderPreviousRouteAdapter);
        routeSearch();
        return v;
    }

    private void routeSearch() {
        activeRoutes.clear();
        previousRoutes.clear();
        routeDrivers.clear();
        u.loadLastRoutes(this::activeRouteDataReturned,this::previousRouteDataReturned);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void activeRouteDataReturned(Route r, User driver){
        activeRoutes.add(r);
//        check if the driver exists
        boolean exists = false;
        for (User u:routeDrivers) {
            if (driver.getUserId().equals(u.getUserId())) {
                exists = true;
                break;
            }
        }
        if (!exists){
            routeDrivers.add(driver);
        }
        riderActiveRouteAdapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void previousRouteDataReturned(Route r, User driver){
        previousRoutes.add(r);
//        check if the driver exists
        boolean exists = false;
        for (User u:routeDrivers) {
            if (driver.getUserId().equals(u.getUserId())) {
                exists = true;
                break;
            }
        }
        if (!exists){
            routeDrivers.add(driver);
        }
        riderPreviousRouteAdapter.notifyDataSetChanged();
    }
}