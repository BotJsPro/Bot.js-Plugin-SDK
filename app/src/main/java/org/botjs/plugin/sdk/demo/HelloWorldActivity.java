package org.botjs.plugin.sdk.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloWorldActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        String message = getIntent().getStringExtra("message");
        TextView tv = findViewById(R.id.textView);
        tv.setText(message != null ? message : "HelloWorldActivity");
    }
}