package com.htw.warik.jsontesthtwdd;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView texView = new TextView(getApplicationContext());
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        texView.setText("Test");
        texView.setWidth(200);
        texView.setHeight(200);
        texView.setTextColor(Color.parseColor("#000000"));
        Json json = new Json(getApplicationContext());
        texView.setText(json.getJahrsem());
        relativeLayout.addView(texView);

    }
}
