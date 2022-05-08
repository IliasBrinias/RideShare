package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.RouteDateTime;
import com.unipi.diplomaThesis.rideshare.Model.RouteLatLng;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this mapFragmentView.
 */
public class DriverSaveRouteFragment extends Fragment {

    SeekBar seekBarProgress;
    View fragment;
    public DriverSaveRouteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }
    private View thumbView;
    private DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    private DriverSaveRouteAdditionalInfoFragment driverSaveRouteAdditionalInfoFragment;
    private DriverSaveRouteDateTimeFragment driverSaveRouteDateTimeFragment;
    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    public View onCreateView(LayoutInflater in, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mapFragmentView
        View v = in.inflate(R.layout.driver_save_route_fragment, container, false);
        thumbView = LayoutInflater.from(getContext()).inflate(R.layout.layout_seekbar_thumb,null,false);
//        initialize the fragments
        driverSaveRouteDirectionsFragment = new DriverSaveRouteDirectionsFragment();
        driverSaveRouteAdditionalInfoFragment = new DriverSaveRouteAdditionalInfoFragment();
        driverSaveRouteDateTimeFragment = new DriverSaveRouteDateTimeFragment();

        fragment = v.findViewById(R.id.fragmentSaveRouteSteps);
        fragment.setFocusableInTouchMode(true);
        fragment.requestFocus();
        fragment.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event)
            {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_DOWN)
                {
                   stepBack();
                   return true;
                }
                return false;
            }
        } );
        seekBarProgress = v.findViewById(R.id.seekBarProgress);
        seekBarProgress.setOnTouchListener((view, motionEvent) -> true);
        changeStepFragment(0);
        return v;
    }
    public void addStep(){
        changeStepFragment(seekBarProgress.getProgress() + 1);
    }
    public void stepBack(){
        if (seekBarProgress.getProgress() - 1 < 0){
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,new DriverRouteFragment()).commit();
            return;
        }else if (seekBarProgress.getProgress() == 2){
            changeStepFragment(seekBarProgress.getProgress() - 2);
            return;
        }
        changeStepFragment(seekBarProgress.getProgress() - 1);
    }

    private void changeStepFragment(int step){
        seekBarProgress.setProgress(step);
        seekBarProgress.setThumb(getThumb(step+1));

        switch (step){
            case 0:
                driverSaveRouteDirectionsFragment = new DriverSaveRouteDirectionsFragment();
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentSaveRouteSteps, driverSaveRouteDirectionsFragment).commit();
                break;
            case 2:
                driverSaveRouteDateTimeFragment = new DriverSaveRouteDateTimeFragment();
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentSaveRouteSteps,driverSaveRouteDateTimeFragment).commit();
                break;
            case 3:
                driverSaveRouteAdditionalInfoFragment = new DriverSaveRouteAdditionalInfoFragment();
                getChildFragmentManager().beginTransaction().replace(R.id.fragmentSaveRouteSteps,driverSaveRouteAdditionalInfoFragment).commit();
                break;
        }

    }
    public Drawable getThumb(int progress) {

        ((TextView) thumbView.findViewById(R.id.tvProgress)).setText(progress + "");

        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);

        return new BitmapDrawable(getResources(), bitmap);
    }
    public void saveRoute(){
        Driver driver = null;
        try {
            driver = (Driver) User.loadUserInstance(getActivity());
        }catch (ClassCastException e){
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
        }
//        collect all the data from the fragments and create the route Object
        RouteLatLng points = driverSaveRouteDirectionsFragment.getPoints();
        RouteDateTime routeDateTime = driverSaveRouteDateTimeFragment.getRouteDateTime();
        Map<String,String> additionalInfo = driverSaveRouteAdditionalInfoFragment.getAdditionalInfo();
        Route r = new Route(null,
                driver.getUserId(),
                points,
                null,
                routeDateTime,
                additionalInfo.get("price"),
                additionalInfo.get("maxPassengers"),
                additionalInfo.get("description")
        );
        driver.saveRoute(r, getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,new DriverRouteFragment()).commit();
                    Toast.makeText(getActivity(), getString(R.string.route_saved_success),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}