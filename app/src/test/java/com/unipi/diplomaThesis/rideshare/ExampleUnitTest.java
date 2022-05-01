package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.text.SimpleDateFormat;
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
    public void addition_isCorrect() throws JSONException {
//        Calendar now = new GregorianCalendar();
//        now.setTimeInMillis(System.currentTimeMillis());
//        long before = now.getTimeInMillis();
//        for (int i=0; i<20;i++){
//            now.roll(Calendar.MONTH,1);
//        }
//        long after = now.getTimeInMillis();
//        System.out.println(before+" "+after);
//        System.out.println(before<=after);
        Date d = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        System.out.println(simpleDateFormat.format(d.getTime()));
    }
}