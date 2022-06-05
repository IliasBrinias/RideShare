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
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1");
        arrayList.add("2");
        arrayList.add("3");
        arrayList.add("4");
        arrayList.add("5");
        for (String s:arrayList){
            if (arrayList.indexOf(s) == arrayList.size()-1){
                System.out.println("!!!!!!!!!!!!!!!!");
            }
            if (s.equals(arrayList.get(arrayList.size()-1))){
                System.out.println("sssssssssssssssssssssssss");
            }
        }
    }
}