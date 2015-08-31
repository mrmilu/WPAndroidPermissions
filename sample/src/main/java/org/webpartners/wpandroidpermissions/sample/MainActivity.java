package org.webpartners.wpandroidpermissions.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button)
    public void item1() {
        startActivity(new Intent(MainActivity.this, ActivityWithPermission.class));
    }

    @OnClick(R.id.button2)
    public void item2() {
        startActivity(new Intent(MainActivity.this, ActivityFragmentHost.class));
    }
}
