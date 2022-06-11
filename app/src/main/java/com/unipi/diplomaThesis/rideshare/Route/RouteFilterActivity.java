package com.unipi.diplomaThesis.rideshare.Route;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.unipi.diplomaThesis.rideshare.Model.RouteFilter;
import com.unipi.diplomaThesis.rideshare.R;

import java.math.BigDecimal;
import java.util.Locale;

public class RouteFilterActivity extends AppCompatActivity implements RangeSlider.OnChangeListener {
//    float minPrice
    RadioGroup radioGroupRepeatability,
               radioGroupClassification;
    EditText minPrice, maxPrice,
            minTime, maxTime,
            minRating,maxRating;
    RangeSlider rangeSliderPrice,
                rangeSliderTime,
                rangeSliderRating;
    TableRow tableRowPriceTitle, tableRowPrice,
            tableRowRepeatabilityTitle, tableRowRepeatability,
            tableRowTimeTitle, tableRowTime,
            tableRowRatingTitle, tableRowRating,
            tableRowClassificationTitle, tableRowClassification;
    ImageView imagePrice,
            imageRepeatability,
            imageTime,
            imageClassification,
            imageRating;
    TextView clear;
    static RouteFilter routeFilter = new RouteFilter();
    private static final int ROTATE_DURATION = 250;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_filter);
        routeFilter = (RouteFilter) getIntent().getSerializableExtra(RouteFilter.class.getSimpleName());
        if (routeFilter == null) finish();

//        table rows
        tableRowPriceTitle = findViewById(R.id.tableRowPriceTitle);
        tableRowPrice = findViewById(R.id.tableRowPrice);
        tableRowRepeatabilityTitle = findViewById(R.id.tableRowRepeatabilityTitle);
        tableRowRepeatability = findViewById(R.id.tableRowRepeatability);
        tableRowTimeTitle = findViewById(R.id.tableRowTimeTitle);
        tableRowClassificationTitle = findViewById(R.id.tableRowClassificationTitle);
        tableRowClassification = findViewById(R.id.tableRowClassification);
        tableRowTime = findViewById(R.id.tableRowTime);
        tableRowRatingTitle = findViewById(R.id.tableRowRatingTitle);
        tableRowRating = findViewById(R.id.tableRowRating);

//        elements in table rows
        radioGroupRepeatability = findViewById(R.id.radioGroupRepeatability);
        radioGroupClassification = findViewById(R.id.radioGroupClassification);
        minPrice = findViewById(R.id.autoCompleteFromPrice);
        maxPrice = findViewById(R.id.autoCompleteToPrice);
        minTime = findViewById(R.id.autoCompleteFromTime);
        maxTime = findViewById(R.id.autoCompleteToTime);
        minRating = findViewById(R.id.autoCompleteFromRating);
        maxRating = findViewById(R.id.autoCompleteToRating);
        rangeSliderPrice = findViewById(R.id.sliderPrice);
        rangeSliderTime = findViewById(R.id.sliderTime);
        rangeSliderRating = findViewById(R.id.sliderRating);
        clear = findViewById(R.id.textViewClear);
//        images
        imagePrice = findViewById(R.id.imageViewPrice);
        imageRepeatability = findViewById(R.id.imageViewRepeatability);
        imageTime = findViewById(R.id.imageViewTime);
        imageRating = findViewById(R.id.imageViewRating);
        imageClassification = findViewById(R.id.imageViewClassification);
//        setListeners
        clear.setOnClickListener(this::clearFilter);
        rangeSliderPrice.addOnChangeListener(this);
        rangeSliderTime.addOnChangeListener(this);
        rangeSliderRating.addOnChangeListener(this);
        tableRowRepeatability.setVisibility(View.GONE);
        tableRowPrice.setVisibility(View.GONE);
        tableRowTime.setVisibility(View.GONE);
        tableRowRating.setVisibility(View.GONE);
        tableRowClassification.setVisibility(View.GONE);
        tableRowClicked(tableRowRepeatability,imageRepeatability);
        tableRowClicked(tableRowPrice,imagePrice);
        tableRowClicked(tableRowTime,imageTime);
        tableRowClicked(tableRowRating,imageRating);
        tableRowClicked(tableRowClassification,imageClassification);
        loadRouteFilter(routeFilter);

    }
    public void openTableRowItems(View view){
        if (view.getId() == tableRowPriceTitle.getId()){
            tableRowClicked(tableRowPrice,imagePrice);
        }else if (view.getId() == tableRowRepeatabilityTitle.getId()){
            tableRowClicked(tableRowRepeatability,imageRepeatability);
        }else if (view.getId() == tableRowTimeTitle.getId()){
            tableRowClicked(tableRowTime,imageTime);
        }else if (view.getId() == tableRowRatingTitle.getId()){
            tableRowClicked(tableRowRating,imageRating);
        }else if (view.getId()==tableRowClassificationTitle.getId()){
            tableRowClicked(tableRowClassification,imageClassification);
        }
    }
    public void closeFilter(View view){
        finish();
    }
    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void clearFilter(View view){
//        repeatability
        radioGroupRepeatability.check(1);
        radioGroupClassification.check(1);
//        setValues to sliders
        rangeSliderPrice.setValues((float) routeFilter.getDefaultMinPrice(),(float) routeFilter.getDefaultMaxPrice());
        minPrice.setText(routeFilter.getDefaultMinPrice() +"€");
        maxPrice.setText(routeFilter.getDefaultMaxPrice() +"€");

        rangeSliderTime.setValues(routeFilter.getDefaultMinTime(),routeFilter.getDefaultMaxTime());
        minTime.setText(String.valueOf(routeFilter.getDefaultMinTime()));
        maxTime.setText(String.valueOf(routeFilter.getDefaultMaxTime()));

        rangeSliderRating.setValues(routeFilter.getDefaultMinRating(),routeFilter.getDefaultMaxRating());
        minRating.setText(String.valueOf(routeFilter.getDefaultMinRating()));
        maxRating.setText(String.valueOf(routeFilter.getDefaultMaxRating()));
//        set variables to default
        routeFilter.setMinPricePerPassenger(routeFilter.getDefaultMinPrice());
        routeFilter.setMaxPricePerPassenger(routeFilter.getDefaultMaxPrice());
        routeFilter.setMinTime(routeFilter.getDefaultMinTime());
        minTime.setText(-routeFilter.getDefaultMinTime() + " " + getString(R.string.hours) + " " + getString(R.string.sooner));
        routeFilter.setMaxTime(routeFilter.getDefaultMaxTime());
        maxTime.setText(routeFilter.getDefaultMaxTime() + " " + getString(R.string.hours) + " " + getString(R.string.later));
        routeFilter.setMinRating(routeFilter.getDefaultMinRating());
        routeFilter.setMaxRating(routeFilter.getDefaultMaxRating());

    }
    private void tableRowClicked(TableRow row, ImageView image){
        RotateAnimation rotate;
        if (row.getVisibility() == View.GONE){
            row.setVisibility(View.VISIBLE);

            rotate = new RotateAnimation(0,
                    180,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }else {
            row.setVisibility(View.GONE);
            rotate = new RotateAnimation(180,
                    0,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        rotate.setFillAfter(true);
        rotate.setDuration(ROTATE_DURATION);
        rotate.setInterpolator(new LinearInterpolator());
        image.startAnimation(rotate);
    }
    @SuppressLint("ResourceType")
    public void applyFilters(View view){
//        repeatability
        routeFilter.setTimetable(getRadioButtonIdx(radioGroupRepeatability));
//        Classification
        routeFilter.setClassification(getRadioButtonIdx(radioGroupClassification));
//        price
        routeFilter.setMinPricePerPassenger(rangeSliderPrice.getValues().get(0));
        routeFilter.setMaxPricePerPassenger(rangeSliderPrice.getValues().get(1));
//        time
        routeFilter.setMinTime(rangeSliderTime.getValues().get(0));
        routeFilter.setMaxTime(rangeSliderTime.getValues().get(1));
//        rating
        routeFilter.setMinRating(rangeSliderRating.getValues().get(0));
        routeFilter.setMaxRating(rangeSliderRating.getValues().get(1));

        Intent i = new Intent();
        i.putExtra(RouteFilter.class.getSimpleName(), routeFilter);
        setResult(Activity.RESULT_OK, i);
        this.finish();
    }
    private int getRadioButtonIdx(RadioGroup radioGroup){
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        return radioGroup.indexOfChild(radioButton);
    }
    @SuppressLint({"RestrictedApi", "SetTextI18n"})
    @Override
    public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        int id = slider.getId();
//        Price
        if (id == rangeSliderPrice.getId()) {
            minPrice.setText(String.format(Locale.getDefault(), "%.2f", slider.getValues().get(0)) + "€");
            maxPrice.setText(String.format(Locale.getDefault(), "%.2f", slider.getValues().get(1)) + "€");
            if (slider.getValues().get(0)< routeFilter.getDefaultMinPrice()){
                slider.setValues((float) routeFilter.getDefaultMinPrice(),slider.getValues().get(1));
            }else if (slider.getValues().get(1)> (float) routeFilter.getDefaultMaxPrice()) {
                System.out.println(slider.getValues().get(1));
                System.out.println((float) routeFilter.getDefaultMaxPrice());
                System.out.println(slider.getValues().get(1)> (float) routeFilter.getDefaultMaxPrice());
                slider.setValues(slider.getValues().get(0), (float) routeFilter.getDefaultMaxPrice());
            }
        }
//        Time
        else if (id == rangeSliderTime.getId()) {
            if (slider.getValues().get(0) >= -0.5)
                slider.setValues(-0.5f, slider.getValues().get(1));
            if (slider.getValues().get(1) <= 0.5)
                slider.setValues(slider.getValues().get(0), 0.5f);
            float minValue = BigDecimal.valueOf(slider.getValues().get(0))
                    .setScale(1,BigDecimal.ROUND_HALF_DOWN)
                    .floatValue();
            float maxValue = BigDecimal.valueOf(slider.getValues().get(1))
                    .setScale(1,BigDecimal.ROUND_HALF_DOWN)
                    .floatValue();
            if (minValue == -1) {
                minTime.setText(1 + " " + getString(R.string.hour) + " " + getString(R.string.sooner));
            } else {
                minTime.setText(-minValue + " " + getString(R.string.hours) + " " + getString(R.string.sooner));
            }
            if (maxValue == 1) {
                maxTime.setText(1 + " " + getString(R.string.hour) + " " + getString(R.string.later));
            } else {
                maxTime.setText(maxValue + " " + getString(R.string.hours) + " " + getString(R.string.later));
            }
        }
//        Rating
        else if (id == rangeSliderRating.getId()) {
            minRating.setText(String.format(Locale.getDefault(), "%.1f", slider.getValues().get(0)));
            maxRating.setText(String.format(Locale.getDefault(), "%.1f", slider.getValues().get(1)));
        }
    }
    private void loadRouteFilter(RouteFilter routeFilter){
//        Repeatability
        if (routeFilter.getTimetable()!=routeFilter.getDefaultTimetable()) {
            radioGroupRepeatability.check(radioGroupRepeatability.getChildAt(routeFilter.getTimetable()).getId());
        }
//        Classification
        if (routeFilter.getClassification()!=routeFilter.getDefaultClassification()) {
            radioGroupClassification.check(radioGroupClassification.getChildAt(routeFilter.getClassification()).getId());
        }
//        Price
        System.out.println(routeFilter.getDefaultMinPrice() +" "+routeFilter.getDefaultMaxPrice());
        rangeSliderPrice.setValues((float) routeFilter.getDefaultMinPrice(),(float) routeFilter.getDefaultMaxPrice());
//        Time
        rangeSliderTime.setValues(routeFilter.getMinTime(),routeFilter.getMaxTime());
//        Rating
        rangeSliderRating.setValues(routeFilter.getMinRating(),routeFilter.getMaxRating());
    }
}