package com.dwarsoftgames.edufun.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dwarsoftgames.edufun.R;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        setOnClicks();
    }

    private void setViews() {
        btLogin = findViewById(R.id.btSave);
    }

    private void setOnClicks() {
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Dashboard.class);
                startActivity(intent);
            }
        });
    }
}
