package org.webpartners.wpandroidpermissions.sample;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.webpartners.wpandroidpermissions.annotations.FragmentWithRuntimePermissions;
import org.webpartners.wpandroidpermissions.annotations.NeedPermissions;
import org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jorge Garrido Oval on 31/08/15.
 */
@FragmentWithRuntimePermissions
public class FragmentWithPermission extends Fragment implements PermissionRequestResponse {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.button)
    @NeedPermissions(Manifest.permission.CAMERA)
    public void openCamera() {
        new FragmentWithPermission_Generated().openCamera();
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
