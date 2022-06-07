package com.unipi.diplomaThesis.rideshare.driver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DriverRouteListFragment extends Fragment {

    private static final int REQ_NEW_ROUTE_ACTIVITY = 123;
    private static final int REQ_EDIT_ROUTE_ACTIVITY = 798;
    private TextView welcomeTitle;
    private RecyclerView recyclerViewDriver;
    private Driver driver;
    private ProgressBar progressBar;
    private List<Route> routeList = new ArrayList<>();
    private DriverRouteListAdapter driverRouteListAdapter;
    TableRow tableRowAddRoute;
    Map<String,Boolean> clicked = new HashMap<>();

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
        progressBar = v.findViewById(R.id.progressBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerViewDriver.setLayoutManager(linearLayoutManager);
        driverRouteListAdapter = new DriverRouteListAdapter(getActivity(), routeList, driver, new OnClickDriverRoute() {
            public void showRoute(int position) {
                Intent i =new Intent(getActivity(), RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),routeList.get(position).getDriverId());
                startActivity(i);

            }
            public void editRoute(int position) {
                DriverRouteListFragment.this.startPostponedEnterTransition();
                Intent i = new Intent(getActivity(), DriverSaveRouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position));
                startActivityForResult(i,REQ_EDIT_ROUTE_ACTIVITY);
                DriverRouteListFragment.this.stopProgressBarAnimation();
            }
            @SuppressLint("NotifyDataSetChanged")
            public void deleteRoute(int position) {
                final View dialogView = View.inflate(getActivity(), R.layout.delete_route_alert_dialog, null);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_dialog_background));
                dialogView.findViewById(R.id.deleteRoute).setOnClickListener(view -> {
                    driver.deleteRoute(routeList.get(position));
                    routeList.remove(position);
                    driverRouteListAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                });
                dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> alertDialog.dismiss());
                alertDialog.setCancelable(true);
                alertDialog.show();
            }

            @Override
            public void itemClick(View v, View slide, ImageView show, ImageView edit, ImageView delete, int position) {
                clicked.putIfAbsent(routeList.get(position).getRouteId(), true);
                if(clicked.get(routeList.get(position).getRouteId())){
                    TranslateAnimation animate0 = new TranslateAnimation(0, -slide.getWidth(), 0, 0);
                    animate0.setDuration(500);
                    animate0.setFillAfter(true);
                    v.startAnimation(animate0);
                    show.setOnClickListener(view -> {
                        startProgressBarAnimation();
                        showRoute(position);
                        stopProgressBarAnimation();
                    });
                    edit.setOnClickListener(view -> {
                        startProgressBarAnimation();
                        editRoute(position);
                        stopProgressBarAnimation();
                    });
                    delete.setOnClickListener(view -> deleteRoute(position));
                    slide.setVisibility(View.VISIBLE);
                    show.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    TranslateAnimation animate1 = new TranslateAnimation(
                            slide.getWidth(),
                            0,
                            0,
                            0);
                    animate1.setDuration(500);
                    animate1.setFillAfter(true);
                    slide.startAnimation(animate1);
                } else {
                    TranslateAnimation animate4 = new TranslateAnimation(-slide.getWidth(), 0, 0, 0);
                    animate4.setDuration(500);
                    animate4.setFillAfter(true);
                    v.startAnimation(animate4);
                    show.setOnClickListener(view -> itemClick(v, slide, show, edit, delete, position));
                    edit.setOnClickListener(view -> itemClick(v, slide, show, edit, delete, position));
                    delete.setOnClickListener(view ->itemClick(v, slide, show, edit, delete, position));
                    slide.setVisibility(View.INVISIBLE);
                    TranslateAnimation animate2 = new TranslateAnimation(
                            0,
                            slide.getWidth(),
                            0,0
                            );
                    animate2.setDuration(500);
                    animate2.setFillAfter(true);
                    slide.startAnimation(animate2);
                }
                clicked.put(routeList.get(position).getRouteId(),!clicked.get(routeList.get(position).getRouteId()));
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
        clicked.clear();
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
        if ((requestCode == REQ_NEW_ROUTE_ACTIVITY || requestCode==REQ_EDIT_ROUTE_ACTIVITY)){
            routeSearch();
        }
    }
}