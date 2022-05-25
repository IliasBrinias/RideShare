package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        List<String> test = new ArrayList<>();
        test.add("a");
        test.add("a");
        test.add("a");
        test.add("a");
        test.add("a");
        System.out.println(test.size());
        System.out.println(test.get(test.size()-1));
    }
}