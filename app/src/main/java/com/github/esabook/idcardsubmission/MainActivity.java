package com.github.esabook.idcardsubmission;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take_card).setOnClickListener(this);
        findViewById(R.id.take_selfie_with_card).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_card:
                startActivity(new Intent(v.getContext(),
                    TakeCardActivity.class));
                break;

            case R.id.take_selfie_with_card:
                startActivity(new Intent(v.getContext(),
                        TakeSelfieWithCardActivity.class));
                break;

        }
    }
}
