package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest extends Activity{
    JSONArray jsonArray = new JSONArray();
    JSONObject jsonObject = new JSONObject();
    @Test
    public void addition_isCorrect(){
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(new Date().getTime());
        System.out.println(c.get(Calendar.DAY_OF_WEEK));
    }
}