package org.webpartners.wpandroidpermissions.sample;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.webpartners.wpandroidpermissions.annotations.ActivityWithRuntimePermissions;
import org.webpartners.wpandroidpermissions.annotations.NeedPermissions;
import org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse;

import butterknife.ButterKnife;
import butterknife.OnClick;

@ActivityWithRuntimePermissions
public class ActivityWithPermission extends AppCompatActivity implements PermissionRequestResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_permission);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button)
    @NeedPermissions(Manifest.permission.CAMERA)
    public void openCamera() {
        Toast.makeText(this, "IT WORKS!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionAllowed() {
        Log.d(getString(R.string.app_name), "Permission granted!");
    }

    @Override
    public void permissionDenied() {
        Log.d(getString(R.string.app_name), "Permission denied!");
    }
}
