package com.santiagogd.spheregradient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;
import android.util.TypedValue;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#0D1117"));
        layout.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText("Sphere Gradient");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("\n3D Sphere Icon Pack\nfor Samsung One UI / Theme Park");
        subtitle.setTextColor(Color.parseColor("#8B949E"));
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        TextView info = new TextView(this);
        info.setText("\n\nInstala este APK y ábrelo en Theme Park\n(Icon → Create New → Iconpack)");
        info.setTextColor(Color.parseColor("#58A6FF"));
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        info.setGravity(Gravity.CENTER);
        layout.addView(info);

        setContentView(layout);
    }
}
