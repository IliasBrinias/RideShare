package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

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
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                System.out.println("A " + i);
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                System.out.println("B " + i);
            }
        });
        t1.start();
        t2.start();
    }


}