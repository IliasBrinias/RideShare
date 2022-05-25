package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.adapter.DriverRouteListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverRouteFragment extends Fragment {
    private RecyclerView recyclerView;
    private static List<Route> routeList = new ArrayList<>();
    private DriverRouteListAdapter driverRouteListAdapter;
    private Driver driver = null;
    public DriverRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mapFragmentView
        View v = inflater.inflate(R.layout.driver_route_fragment, container, false);
        routeList.clear();
        try {
            driver = (Driver) User.loadUserInstance(getActivity());
        }catch (Exception e){
            e.printStackTrace();
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
        }
        if (driver == null) {
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
            return v;
        }
        recyclerView = v.findViewById(R.id.recyclerDriveRoute);
        CircleImageView addRoute = v.findViewById(R.id.buttonAddRoute);
        DriverSaveRouteFragment driverSaveRouteFragment = new DriverSaveRouteFragment();
//        add Route onClick Listener
        addRoute.setOnClickListener(view -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,driverSaveRouteFragment).commit();
        });
//        enable scrolling in a scrollview
        recyclerView.setOnTouchListener((view, event) -> {
            try {
                view.findViewById(R.id.scrollView).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }catch (Exception e){
                return false;
            }
        });
//        initialize recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        driver.loadDriverRoutes(route -> refreshData(route));
        driverRouteListAdapter = new DriverRouteListAdapter(getActivity(), routeList, null, new OnClickDriverRoute() {
            @Override
            public void editDriversRoute(View v, int position) {
//                TODO: edit Route
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void deleteDriversRoute(View v, int position) {
//                Opens an alert box and delete the route if the answer is positive
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.MyAlertDialogStyle));
                alert.setTitle(getString(R.string.delete_route));
                alert.setMessage(getString(R.string.are_you_sure_delete_route));
                alert.setIcon(R.drawable.ic_alert);
                alert.setPositiveButton(getString(R.string.delete), (dialog, which) ->
                {
                    driver.deleteRoute(routeList.get(position),getActivity());
                    driverRouteListAdapter.removeAt(position);
                });
                alert.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
                alert.show();
            }

            @Override
            public void itemClick(View v, int position) {

            }
        });

        recyclerView.setAdapter(driverRouteListAdapter);
        return v;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Route r){
        routeList.add(r);
        driverRouteListAdapter.notifyDataSetChanged();
    }
}