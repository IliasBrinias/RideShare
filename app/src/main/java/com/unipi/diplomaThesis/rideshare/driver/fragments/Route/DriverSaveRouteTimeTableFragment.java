package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.unipi.diplomaThesis.rideshare.Model.RouteDateTime;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.DriverSaveRouteActivity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DriverSaveRouteTimeTableFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    LinearLayout linearLayoutChooseDays, linearLayoutRepeatTypeSelection;
    private Button nextStep;
    //startDate Objects
    private final int MAX_YEAR_PICKER = 2;
    private final int MIN_DAY_PICKER = 1;
    private TextView startDate, endDate,infoMessage;
    private TextInputLayout startDateLayout, endDateLayout;
    private Spinner repeatField;
//  Our user data for the route from this fragments
    private long startDateUnixTime = 0;
    private long endDateUnixTime = 0;
    private Integer timetable = null;
    Map<String,String> days= new HashMap<>();
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switchRepeat,switchSunday,switchMonday,
            switchTuesday,switchWednesday,switchThursday,
            switchFriday,switchSaturday;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat startDateFormat =new SimpleDateFormat("dd/MM/yyyy HH:mm");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat endDateFormat =new SimpleDateFormat("dd/MM/yyyy");

    public DriverSaveRouteTimeTableFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater in, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = in.inflate(R.layout.driver_save_route_date_time_fragment, container, false);
//        rest elements
        startDate = v.findViewById(R.id.textInputStartDate);
        startDateLayout = v.findViewById(R.id.textInputStartDateLayout);
        endDate = v.findViewById(R.id.textInputEndDate);
        endDateLayout = v.findViewById(R.id.textInputEndDateLayout);
        repeatField = v.findViewById(R.id.spinnerRepeat);
        nextStep = v.findViewById(R.id.buttonNextStepDateTime);
        linearLayoutChooseDays = v.findViewById(R.id.linearLayoutChooseDays);
        linearLayoutRepeatTypeSelection = v.findViewById(R.id.linearLayoutRepeatTypeSelection);
        infoMessage = v.findViewById(R.id.infoMessage);
//        initialize the elements visibility
        nextStep.setVisibility(View.GONE);
        endDateLayout.setVisibility(View.GONE);
        linearLayoutRepeatTypeSelection.setVisibility(View.GONE);
        linearLayoutChooseDays.setVisibility(View.GONE);
        infoMessage.setVisibility(View.GONE);

        // load switches
        switchRepeat = v.findViewById(R.id.switchRepeat);
        switchSunday = v.findViewById(R.id.switchRepeatSunday);
        switchMonday = v.findViewById(R.id.switchRepeatMonday);
        switchTuesday = v.findViewById(R.id.switchRepeatTuesday);
        switchWednesday = v.findViewById(R.id.switchRepeatWednesday);
        switchThursday = v.findViewById(R.id.switchRepeatThursday);
        switchFriday = v.findViewById(R.id.switchRepeatFriday);
        switchSaturday = v.findViewById(R.id.switchRepeatSaturday);

        switchRepeat.setOnCheckedChangeListener(this);
        switchSunday.setOnCheckedChangeListener(this);
        switchMonday.setOnCheckedChangeListener(this);
        switchTuesday.setOnCheckedChangeListener(this);
        switchWednesday.setOnCheckedChangeListener(this);
        switchThursday.setOnCheckedChangeListener(this);
        switchFriday.setOnCheckedChangeListener(this);
        switchSaturday.setOnCheckedChangeListener(this);

        repeatField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int item, long id) {
                if (view == null) return;
                timetable = item;
                ((TextView) view).setText(getResources().getStringArray(R.array.repeat_options)[timetable]);
                if (item==1){
                    linearLayoutChooseDays.setVisibility(View.VISIBLE);
                }else {
                    linearLayoutChooseDays.setVisibility(View.GONE);
                }
                validateFields();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        startDate.setOnClickListener(this::setDateTime);
        endDate.setOnClickListener(this::setDateTime);
        nextStep.setOnClickListener(view -> {
            ((DriverSaveRouteActivity) getActivity()).nextStep();
        });
        return v;
    }
    public RouteDateTime getRouteDateTime(){
        if (!days.isEmpty() & repeatField.getSelectedItemPosition()==1){
            Map<String, String> sortedDays = days.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new)
                    );
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(startDateUnixTime);
            c.set(Calendar.DAY_OF_WEEK,Integer.parseInt(sortedDays.values().stream().findFirst().get()));
            return new RouteDateTime(null,startDateUnixTime,switchRepeat.isChecked(),repeatField.getSelectedItemPosition(),days,endDateUnixTime);
        }
        return new RouteDateTime(null,startDateUnixTime,switchRepeat.isChecked(),repeatField.getSelectedItemPosition(),null,endDateUnixTime);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == switchRepeat.getId()){
            if (b){
                linearLayoutRepeatTypeSelection.setVisibility(View.VISIBLE);
                endDateLayout.setVisibility(View.VISIBLE);
                infoMessage.setVisibility(View.VISIBLE);

            }else {
                linearLayoutRepeatTypeSelection.setVisibility(View.GONE);
                endDateLayout.setVisibility(View.GONE);
                infoMessage.setVisibility(View.GONE);
            }
            validateFields();
            return;
        }
        if (b){
            days.put(String.valueOf(compoundButton.getId()),getDayOfWeek(compoundButton.getId()).toString());
            if (nextStep.getVisibility()==View.GONE) nextStep.setVisibility(View.VISIBLE);
        }else {
            days.remove(String.valueOf(compoundButton.getId()),getDayOfWeek(compoundButton.getId()));
            if (days.isEmpty()) nextStep.setVisibility(View.GONE);
        }
        validateFields();
    }
    private Integer getDayOfWeek(int id){
        if (id == switchSunday.getId()) {
            return 1;
        }else if (id == switchMonday.getId()){
            return 2;
        }else if (id == switchTuesday.getId()){
            return 3;
        }else if (id == switchWednesday.getId()){
            return 4;
        }else if (id == switchThursday.getId()){
            return 5;
        }else if (id == switchFriday.getId()){
            return 6;
        }else if (id == switchSaturday.getId()){
            return 7;
        }
        return -1;
    }
    public void setDateTime(View view){
        //load the custom startDate Picker to the alertDialog
        final View dialogView = View.inflate(getActivity(), R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_dialog_background));
        Calendar c = Calendar.getInstance();
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        datePicker.setMinDate(c.getTimeInMillis());
        c.add(Calendar.YEAR,1);
        datePicker.setMaxDate(c.getTimeInMillis());
        timePicker.setVisibility(View.GONE);
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getId() == startDate.getId()) {
                    if (datePicker.getVisibility() == View.VISIBLE) {
                        datePicker.setVisibility(View.GONE);
                        timePicker.setVisibility(View.VISIBLE);
                    } else if (timePicker.getVisibility() == View.VISIBLE) {
                        startDateUnixTime = getDate(datePicker, timePicker).getTimeInMillis();
                        startDate.setText(startDateFormat.format(startDateUnixTime));
                        alertDialog.dismiss();
                        validateFields();

                    }
                } else if (view.getId() == endDate.getId()) {
                    if (datePicker.getVisibility() == View.VISIBLE) {
                        endDateUnixTime = getDate(datePicker, timePicker).getTimeInMillis();
                        endDate.setText(endDateFormat.format(endDateUnixTime));
                        alertDialog.dismiss();
                        validateFields();
                    }
                }
            }
            private GregorianCalendar getDate(DatePicker datePicker, TimePicker timePicker){
                return new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getHour(),
                        timePicker.getMinute());
            }
        });
        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
    private void validateFields(){
        if (startDateUnixTime!=0){
            if (switchRepeat.isChecked()){
                if (endDateUnixTime!=0){
                    Calendar start = new GregorianCalendar();
                    Calendar end = new GregorianCalendar();
                    start.setTimeInMillis(startDateUnixTime);
                    end.setTimeInMillis(endDateUnixTime);
                    Duration duration = Duration.between(start.toInstant(),end.toInstant());

                    if (repeatField.getSelectedItemPosition() == 0) {
                        nextStep.setVisibility(View.VISIBLE);
                        if (Math.abs(duration.toDays())<=1){
                            endDateUnixTime=0;
                            endDate.setText("");
                            Toast.makeText(getActivity(), getActivity().getString(R.string.end_date_error_daily),Toast.LENGTH_SHORT).show();
                            nextStep.setVisibility(View.GONE);

                        }
                    }
                    else if (repeatField.getSelectedItemPosition()==1) {
                        if (Math.abs(duration.toDays())<7){
                            endDateUnixTime=0;
                            endDate.setText("");
                            Toast.makeText(getActivity(), getActivity().getString(R.string.end_date_error_week),Toast.LENGTH_SHORT).show();
                            nextStep.setVisibility(View.GONE);
                            return;
                        }
                        if (!days.isEmpty()){
                            nextStep.setVisibility(View.VISIBLE);
                        }else {
                            nextStep.setVisibility(View.GONE);
                        }
                    }
                    else {
                        nextStep.setVisibility(View.GONE);
                    }

                }else {
                    nextStep.setVisibility(View.GONE);
                }
            }else{
                nextStep.setVisibility(View.VISIBLE);
            }
        }else {
            nextStep.setVisibility(View.GONE);
        }
    }
}