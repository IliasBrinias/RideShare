package com.unipi.diplomaThesis.rideshare.driver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.Route.RouteActivity;
import com.unipi.diplomaThesis.rideshare.driver.adapter.DriverRouteListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DriverRouteListFragment extends Fragment {

    private static final int REQ_NEW_ROUTE_ACTIVITY = 123;
    private TextView welcomeTitle;
    private RecyclerView recyclerViewDriver;
    private Driver driver;
    private ProgressBar progressBar;
    private List<Route> routeList = new ArrayList<>();
    private DriverRouteListAdapter driverRouteListAdapter;
    TableRow tableRowAddRoute;
    public DriverRouteListFragment() {
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
        try {
            driver = (Driver) User.loadUserInstance(getActivity());
        }catch (ClassCastException classCastException){
            classCastException.printStackTrace();
            getActivity().finish();
        }
        welcomeTitle = v.findViewById(R.id.textViewWelcomeDriverTitle);
        tableRowAddRoute = v.findViewById(R.id.tableRowAddRoute);
        recyclerViewDriver = v.findViewById(R.id.recyclerViewDriverRouteList);
        progressBar = v.findViewById(R.id.progress_bar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerViewDriver.setLayoutManager(linearLayoutManager);
        driverRouteListAdapter = new DriverRouteListAdapter(getActivity(), routeList, driver, new OnClickDriverRoute() {
            @Override
            public void editDriversRoute(View v, int position) {

            }

            @Override
            public void deleteDriversRoute(View v, int position) {

            }

            @Override
            public void itemClick(View v, int position) {
                Intent i =new Intent(getActivity(), RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),routeList.get(position).getDriverId());
                startActivity(i);
            }
        });
        recyclerViewDriver.setAdapter(driverRouteListAdapter);
        tableRowAddRoute.setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), DriverSaveRouteActivity.class),REQ_NEW_ROUTE_ACTIVITY));
        routeSearch();
        initializeTitle();
        return v;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void routeSearch() {
        startProgressBarAnimation();
        routeList.clear();
        driverRouteListAdapter.notifyDataSetChanged();
        driver.loadDriverRoutes(this::returnData);
    }
    private void startProgressBarAnimation(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void stopProgressBarAnimation(){
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void returnData(Route r){
        stopProgressBarAnimation();
        if (r==null) return;
        routeList.add(r);
        driverRouteListAdapter.notifyDataSetChanged();
    }
    @SuppressLint("SetTextI18n")
    private void initializeTitle(){
        String[] name =driver.getFullName().split(" ");
        if (name.length!=2){
            welcomeTitle.setText("Hello "+driver.getFullName()+"!");
        }else {
            welcomeTitle.setText("Hello "+name[0]+"!");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_NEW_ROUTE_ACTIVITY && resultCode == Activity.RESULT_OK){
            routeSearch();
        }
    }
}