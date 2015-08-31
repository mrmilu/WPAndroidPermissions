package org.webpartners.wpandroidpermissions.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.webpartners.wpandroidpermissions.annotations.HostFragmentWithPermissions;


@HostFragmentWithPermissions
public class ActivityFragmentHost extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_fragment);
        changeFragment(getSupportFragmentManager(), new FragmentWithPermission());
    }

    public static void changeFragment(FragmentManager fragmentManager, Fragment targetFragment){
        fragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
