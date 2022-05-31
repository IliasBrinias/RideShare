package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;

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
        ArrayList<String> s1 = new ArrayList<>();
        s1.add("a");
        s1.add("b");
        s1.add("c");
        s1.add("d");
        ArrayList<String> s2 = new ArrayList<>();
        s2.add("e");
        s2.add("j");
        s2.add("c");
        System.out.println(s1.contains("c"));
    }
}