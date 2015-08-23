package org.webpartners.wpandroidpermissions.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.webpartners.wpandroidpermissions.annotations.HasRuntimePermissions;
import org.webpartners.wpandroidpermissions.annotations.NeedPermissions;
import org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse;
import org.webpartners.wpandroidpermissions.processor.PermissionParser;

@HasRuntimePermissions
public class MainActivity extends AppCompatActivity implements PermissionRequestResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionParser.getInstance().parse(this);
    }

    @NeedPermissions(Manifest.permission.CAMERA)
    public void openCamera() {
        Log.d(getString(R.string.app_name), "IT WORKS!");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (this instanceof PermissionRequestResponse)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ((PermissionRequestResponse)this).permissionAllowed();
            else
                ((PermissionRequestResponse)this).permissionDenied();
    }

    @Override
    public void permissionAllowed() {
        Log.d(getString(R.string.app_name), "Permission granted!");
        openCamera();
    }

    @Override
    public void permissionDenied() {
        Log.d(getString(R.string.app_name), "Permission denied!");
    }
}
