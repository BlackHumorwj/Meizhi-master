package com.example.demo_01;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import widget.PieData;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PieView pieView = (PieView) findViewById(R.id.pie_view);
        PieData pieData = new PieData(65);
        PieData pieData1 = new PieData(5);
        PieData pieData2 = new PieData(65);
        ArrayList<PieData> list = new ArrayList<>();
        list.add(pieData);
        list.add(pieData1);
        list.add(pieData2);
        assert pieView != null;
        pieView.setData(list);
    }
}
