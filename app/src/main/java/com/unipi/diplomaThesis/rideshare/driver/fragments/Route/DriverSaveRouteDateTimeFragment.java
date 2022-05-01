package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.unipi.diplomaThesis.rideshare.Model.RouteDateTime;
import com.unipi.diplomaThesis.rideshare.R;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverSaveRouteDateTimeFragment extends Fragment {
    private TableRow tableRowStartDateRoute, tableRowEndDate, tableRowNextStep, tableRowChooseDay;
    private Button nextStep;
    private final List<ToggleButton> toggleButtonsDays = new ArrayList<>();
    private DriverSaveRouteFragment driverSaveRouteFragment;
    //date Objects
    private final int MAX_YEAR_PICKER = 2;
    private final int MIN_DAY_PICKER = 1;
    private Calendar myCalendar= Calendar.getInstance();
    private Calendar combinedCalendar = new GregorianCalendar();
    private SimpleDateFormat dateFormat, timeFormat;
    private TextView date, time, endDate;
    private TextInputLayout dateLayout, timeLayout, endDateLayout;
    private Spinner repeatField;
//  Our user data for the route from this fragments
    private long startDateUnixTime = 0;
    private long endDateUnixTime = 0;
    private long timeUnixTime = 0;
    private Integer repeatness = null;
    private Map<String,String> daysSelected = new HashMap<>();

    public DriverSaveRouteDateTimeFragment() {
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
//        rows
        tableRowStartDateRoute = v.findViewById(R.id.tableRowStartDateRoute);
        tableRowChooseDay = v.findViewById(R.id.tableRowChooseDay);
        tableRowEndDate = v.findViewById(R.id.tableRowEndDate);
        tableRowNextStep = v.findViewById(R.id.tableRowNextStepDateTime);
//        rest elements
        date = v.findViewById(R.id.textInputStartDate);
        dateLayout = v.findViewById(R.id.textInputStartDateLayout);
        time = v.findViewById(R.id.textInputStartTime);
        timeLayout = v.findViewById(R.id.textInputStartTimeLayout);
        endDate = v.findViewById(R.id.textInputEndDate);
        endDateLayout = v.findViewById(R.id.textInputEndDateLayout);
        repeatField = v.findViewById(R.id.spinnerRepeat);
        nextStep = v.findViewById(R.id.buttonNextStepDateTime);
//        initialize the elements visibility
        nextStep.setVisibility(View.GONE);
        tableRowChooseDay.setVisibility(View.GONE);
        tableRowStartDateRoute.setVisibility(View.VISIBLE);
        tableRowEndDate.setVisibility(View.GONE);
//        load daysToggleButtons into the list
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonSN));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonM));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonT));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonW));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonTH));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonF));
        toggleButtonsDays.add(v.findViewById(R.id.toggleButtonST));
        View.OnClickListener toggleTriggerChecked = view -> {
            if (daysSelected.containsKey(String.valueOf(view.getId()))){
//                if the day was selected unchecked it
                daysSelected.remove(String.valueOf(view.getId()));
                if (daysSelected.isEmpty()){
//                  if the user don't choose at least one day the row will disappear
                    tableRowChooseDay.setVisibility(View.GONE);
                    repeatField.setSelection(0);
                }
            }else {
                if (daysSelected.isEmpty()) {
//                  add step and make date step visible
                    driverSaveRouteFragment.addStep();
                    tableRowStartDateRoute.setVisibility(View.VISIBLE);
                    tableRowNextStep.setVisibility(View.VISIBLE);
                }
                daysSelected.put(String.valueOf(view.getId()),getToggleButtonDay(view.getId()));
                if (tableRowNextStep.getVisibility() == View.GONE) tableRowNextStep.setVisibility(View.VISIBLE);
            }
        };
        for (ToggleButton day:toggleButtonsDays) {
            day.setOnClickListener(toggleTriggerChecked);
        }
        repeatField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int item, long id) {
                if (view == null) return;
                repeatness = item;
                ((TextView) view).setText(getResources().getStringArray(R.array.repeat_options)[repeatness]);

                if (item != 0) {
                    tableRowEndDate.setVisibility(View.VISIBLE);
                    daysSelected.clear();
                }else {
                    tableRowEndDate.setVisibility(View.GONE);
                }
                if (getResources().getStringArray(R.array.repeat_options).length-1 == item){
                    tableRowChooseDay.setVisibility(View.VISIBLE);
//                    find the specific day and checked it
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(startDateUnixTime);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    toggleButtonsDays.get(dayOfWeek-1).setChecked(true);
                    int selectedDayButtonId = toggleButtonsDays.get(dayOfWeek-1).getId();
                    daysSelected.put(String.valueOf(selectedDayButtonId),getToggleButtonDay(selectedDayButtonId));
                }else {
                    tableRowChooseDay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        driverSaveRouteFragment = (DriverSaveRouteFragment) this.getParentFragment();
//      Date Objects initialize
        timeFormat=new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat=new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
//        initialize default endDate
        Calendar end = new GregorianCalendar();
        end.setTimeInMillis(new Date().getTime());
        end.roll(Calendar.MONTH,6);
        endDate.setText(dateFormat.format(end.getTimeInMillis()));

        date.setOnClickListener(this::dateTimePicker);
        time.setOnClickListener(this::dateTimePicker);
        endDate.setOnClickListener(this::dateTimePicker);
        nextStep.setOnClickListener(view -> {
            boolean checkForNullFields = false;
            if (startDateUnixTime==0){
                dateLayout.setHintTextColor(ColorStateList.valueOf(Color.RED));
                checkForNullFields = true;
            }
            if (timeUnixTime==0){
                checkForNullFields = true;
            }else {
                time.setError(null);
            }
            if (checkForNullFields) return;
            if (endDateUnixTime == 0 && repeatField.getSelectedItemPosition() != 0){
                return;
            }else {
                endDate.setError(null);
            }
            driverSaveRouteFragment.addStep();
        });
        return v;
    }
    @SuppressLint("NonConstantResourceId")
    private String getToggleButtonDay(int buttonId){
        switch (buttonId){
            case R.id.toggleButtonSN:
                return "0";
            case R.id.toggleButtonM:
                return "1";
            case R.id.toggleButtonT:
                return "2";
            case R.id.toggleButtonW:
                return "3";
            case R.id.toggleButtonTH:
                return "4";
            case R.id.toggleButtonF:
                return "5";
            case R.id.toggleButtonST:
                return "6";
        }
        return "0";
    }
    public void dateTimePicker(View view){
        if (view.getId() == R.id.textInputStartDate){
//            START DATE
            DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    myCalendar.set(Calendar.YEAR, y);
                    myCalendar.set(Calendar.MONTH,m);
                    myCalendar.set(Calendar.DAY_OF_MONTH,d);
                    startDateUnixTime = myCalendar.getTimeInMillis();
                    date.setText(dateFormat.format(new Date(startDateUnixTime)));
                    myCalendar.roll(Calendar.MONTH,6);
                    endDate.setText(dateFormat.format(myCalendar.getTimeInMillis()));
                    if (timeUnixTime!=0){
                        nextStep.setVisibility(View.VISIBLE);
                    }
                }
            };
            myCalendar = new GregorianCalendar();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), startDateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            myCalendar.add(Calendar.DAY_OF_MONTH,MIN_DAY_PICKER);
            datePickerDialog.getDatePicker().setMinDate(myCalendar.getTimeInMillis());
            myCalendar.add(Calendar.YEAR, MAX_YEAR_PICKER);
            datePickerDialog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
            datePickerDialog.show();
        }else if (view.getId() == R.id.textInputStartTime){
//            START TIME
            TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int h, int m) {
                    myCalendar.set(Calendar.HOUR, h);
                    myCalendar.set(Calendar.MINUTE,m);
                    timeUnixTime = myCalendar.getTimeInMillis();
                    time.setText(timeFormat.format(new Date(timeUnixTime)));
                    combinedCalendar.set(Calendar.HOUR_OF_DAY,h);
                    combinedCalendar.set(Calendar.MINUTE,m);
                    if (startDateUnixTime!=0){
                        nextStep.setVisibility(View.VISIBLE);
                    }
                }
            };
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),timeSetListener,
                    myCalendar.get(Calendar.HOUR),
                    myCalendar.get(Calendar.MINUTE),false);
            timePickerDialog.show();
        }else if (view.getId() == R.id.textInputEndDate){
//          END DATE
            DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    myCalendar.set(Calendar.YEAR, y);
                    myCalendar.set(Calendar.MONTH,m);
                    myCalendar.set(Calendar.DAY_OF_MONTH,d);
                    endDateUnixTime = myCalendar.getTimeInMillis();
                    endDate.setText(dateFormat.format(new Date(endDateUnixTime)));
                }
            };
//          load the start date unix time and calculate the minimum endDate and maximum
            Date startDate = new Date(startDateUnixTime);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);
            calendar = calendarRoll(calendar);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), endDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            calendar.add(Calendar.DAY_OF_MONTH,MIN_DAY_PICKER);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            calendar.add(Calendar.YEAR, MAX_YEAR_PICKER);
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
//          show it
            datePickerDialog.show();
        }
    }
    private Calendar calendarRoll(Calendar calendar){
//        based on repeat option the minimum end date will be changed
        switch (repeatField.getSelectedItemPosition()){
            default:
                break;
            case 1:
                calendar.roll(Calendar.DAY_OF_MONTH,1);
                break;
            case 2:
                calendar.roll(Calendar.DAY_OF_MONTH,6);
                break;
            case 3:
                calendar.roll(Calendar.MONTH,1);
                break;
            case 4:
                calendar.roll(Calendar.YEAR,1);
            case 5:
                calendar.roll(Calendar.DAY_OF_MONTH,6);
                break;
        }
        return calendar;
    }
    public RouteDateTime getRouteDateTime(){
        if (daysSelected.isEmpty()){
            return new RouteDateTime(null,startDateUnixTime,timeUnixTime,repeatField.getSelectedItemPosition(),endDateUnixTime);
        }else {
            startDateUnixTime = checkStartDate();
            if (daysSelected.size() == 1){
//                if the driver choose only one day transform it to weekly
                repeatness = 2;
                return new RouteDateTime(null,startDateUnixTime,timeUnixTime,repeatness,daysSelected,endDateUnixTime);
            }else if (daysSelected.size() == 6){
//                if the driver choose only one day transform it to daily
                repeatness = 1;
                return new RouteDateTime(null,startDateUnixTime,timeUnixTime,repeatness,daysSelected,endDateUnixTime);
            }
            return new RouteDateTime(null,startDateUnixTime,timeUnixTime,repeatField.getSelectedItemPosition(),daysSelected,endDateUnixTime);
        }
    }
    private long checkStartDate(){
//       check if the start date day is the same with the selected day
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startDateUnixTime);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int firstDaySelected = Integer.parseInt(Collections.min(daysSelected.values()));
        if (firstDaySelected!=dayOfWeek - 1) {
            if (firstDaySelected > dayOfWeek - 1) {
                calendar.add(Calendar.DAY_OF_YEAR, dayOfWeek - 1 - firstDaySelected);
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, firstDaySelected - (dayOfWeek - 1));
            }
            return calendar.getTimeInMillis();
        }
        return calendar.getTimeInMillis();
    }
}