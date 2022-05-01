package com.unipi.diplomaThesis.rideshare;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.unipi.diplomaThesis.rideshare.Model.RouteFilter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

public class RouteFilterActivity extends AppCompatActivity implements RangeSlider.OnChangeListener {
//    float minPrice
    RadioGroup radioGroupRepeatability;
    EditText minPrice, maxPrice,
            minTime, maxTime,
            minRating,maxRating;
    RangeSlider rangeSliderPrice,
                rangeSliderTime,
                rangeSliderRating;
    TableRow tableRowPriceTitle, tableRowPrice,
            tableRowRepeatabilityTitle, tableRowRepeatability,
            tableRowTimeTitle, tableRowTime,
            tableRowRatingTitle, tableRowRating;
    ImageView imagePrice,
            imageRepeatability,
            imageTime,
            imageRating;
    RouteFilter routeFilter = new RouteFilter();
    private static final int ROTATE_DURATION = 250;
    DecimalFormat df = new DecimalFormat("#.#");
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_filter);
//        table rows
        tableRowPriceTitle = findViewById(R.id.tableRowPriceTitle);
        tableRowPrice = findViewById(R.id.tableRowPrice);
        tableRowRepeatabilityTitle = findViewById(R.id.tableRowRepeatabilityTitle);
        tableRowRepeatability = findViewById(R.id.tableRowRepeatability);
        tableRowTimeTitle = findViewById(R.id.tableRowTimeTitle);
        tableRowTime = findViewById(R.id.tableRowTime);
        tableRowRatingTitle = findViewById(R.id.tableRowRatingTitle);
        tableRowRating = findViewById(R.id.tableRowRating);
//        elements in table rows
        radioGroupRepeatability = findViewById(R.id.radioGroupRepeatability);
        minPrice = findViewById(R.id.autoCompleteFromPrice);
        maxPrice = findViewById(R.id.autoCompleteToPrice);
        minTime = findViewById(R.id.autoCompleteFromTime);
        maxTime = findViewById(R.id.autoCompleteToTime);
        minRating = findViewById(R.id.autoCompleteFromRating);
        maxRating = findViewById(R.id.autoCompleteToRating);
        rangeSliderPrice = findViewById(R.id.sliderPrice);
        rangeSliderTime = findViewById(R.id.sliderTime);
        rangeSliderRating = findViewById(R.id.sliderRating);
//        images
        imagePrice = findViewById(R.id.imageViewPrice);
        imageRepeatability = findViewById(R.id.imageViewRepeatability);
        imageTime = findViewById(R.id.imageViewTime);
        imageRating = findViewById(R.id.imageViewRating);
//        setValues to sliders
        rangeSliderPrice.setValues(0.0f,100.0f);
        rangeSliderTime.setValues(-12.0f,12.0f);
        rangeSliderRating.setValues(0.0f,5.0f);
        minPrice.setText(0.0f +"$");
        maxPrice.setText(100.0f +"$");
//        setListeners
        rangeSliderPrice.addOnChangeListener(this);
        rangeSliderTime.addOnChangeListener(this);
        rangeSliderRating.addOnChangeListener(this);
        tableRowRepeatability.setVisibility(View.GONE);
        tableRowPrice.setVisibility(View.GONE);
        tableRowTime.setVisibility(View.GONE);
        tableRowRating.setVisibility(View.GONE);
        tableRowClicked(tableRowRepeatability,imageRepeatability);
        tableRowClicked(tableRowPrice,imagePrice);
        tableRowClicked(tableRowTime,imageTime);
        tableRowClicked(tableRowRating,imageRating);
        routeFilter = (RouteFilter) getIntent().getSerializableExtra(RouteFilter.class.getSimpleName());
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
        }
    }
    public void closeFilter(View view){
        finish();
    }
    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void clearFilter(View view){
//        repeatability
        radioGroupRepeatability.check(1);
//        price
        minPrice.setText(0.00+"$");
        maxPrice.setText(100.00+"$");
        rangeSliderPrice.setValues(0.0f,100.0f);
//        Time
        minTime.setText(12 +" "+getString(R.string.hours)+" "+ getString(R.string.away));
        maxTime.setText(12 +" "+getString(R.string.hours)+" "+ getString(R.string.after));
        rangeSliderTime.setValues(-12.0f,12.0f);
//        Rating
        minRating.setText(0+"");
        maxRating.setText(5+"");
        rangeSliderRating.setValues(0.0f,5.0f);

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
        int radioButtonID = radioGroupRepeatability.getCheckedRadioButtonId();
        View radioButton = radioGroupRepeatability.findViewById(radioButtonID);
        int idxRepeatability = radioGroupRepeatability.indexOfChild(radioButton);
//        repeatability
        routeFilter.setRepeatability(idxRepeatability);
//        price
        if (rangeSliderPrice.getValues().get(0) == rangeSliderPrice.getValueFrom() &&
                rangeSliderPrice.getValues().get(1) == rangeSliderPrice.getValueTo()){
            routeFilter.setMinPricePerPassenger(-1.f);
            routeFilter.setMaxPricePerPassenger(-1.f);
        }else {
            routeFilter.setMinPricePerPassenger(rangeSliderPrice.getValues().get(0));
            routeFilter.setMaxPricePerPassenger(rangeSliderPrice.getValues().get(1));
        }
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

    @SuppressLint({"RestrictedApi", "SetTextI18n"})
    @Override
    public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        int id = slider.getId();
//        Price
        if (id == rangeSliderPrice.getId()) {
            minPrice.setText(String.format(Locale.getDefault(), "%.2f", slider.getValues().get(0)) + "$");
            maxPrice.setText(String.format(Locale.getDefault(), "%.2f", slider.getValues().get(1)) + "$");
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
                minTime.setText(1 + " " + getString(R.string.hour) + " " + getString(R.string.away));
            } else {
                minTime.setText(-minValue + " " + getString(R.string.hours) + " " + getString(R.string.away));
            }
            if (maxValue == 1) {
                maxTime.setText(1 + " " + getString(R.string.hour) + " " + getString(R.string.after));
            } else {
                maxTime.setText(maxValue + " " + getString(R.string.hours) + " " + getString(R.string.after));
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
        if (routeFilter.getRepeatability()!=-1) {
            radioGroupRepeatability.check(radioGroupRepeatability.getChildAt(routeFilter.getRepeatability()).getId());
        }
//        Price
        if (routeFilter.getMinPricePerPassenger() != -1.f || routeFilter.getMaxPricePerPassenger() != -1.f) {
            rangeSliderPrice.setValues(routeFilter.getMinPricePerPassenger(),routeFilter.getMaxPricePerPassenger());
        }
//        Time
        rangeSliderTime.setValues(routeFilter.getMinTime(),routeFilter.getMaxTime());
//        Rating
        rangeSliderRating.setValues(routeFilter.getMinRating(),routeFilter.getMaxRating());
    }
}