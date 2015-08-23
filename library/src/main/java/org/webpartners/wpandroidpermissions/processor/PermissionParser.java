package org.webpartners.wpandroidpermissions.processor;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.webpartners.wpandroidpermissions.annotations.HasRuntimePermissions;
import org.webpartners.wpandroidpermissions.annotations.NeedPermissions;
import org.webpartners.wpandroidpermissions.interfaces.PermissionRequestResponse;

import java.lang.reflect.Method;

/**
 * Created by Jorge Garrido Oval on 13/05/15.
 */
public class PermissionParser {

    private AppCompatActivity act;
    private final int CODE = 28745;

    /*
    INIT SINGLETON
     */
    private static PermissionParser INSTANCE = null;

    private PermissionParser(){}

    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized(PermissionParser.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PermissionParser();
                }
            }
        }
    }

    public static PermissionParser getInstance() {
        if (INSTANCE == null) createInstance();
        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    /*
    END SINGLETON
     */

    public void parse(AppCompatActivity obj) {
        if (obj instanceof PermissionRequestResponse) {
            act = obj;
            execute((PermissionRequestResponse) obj);
        }
    }

    public void parse(Fragment obj) {
        if (obj instanceof PermissionRequestResponse) {
            act = (AppCompatActivity) obj.getActivity();
            execute((PermissionRequestResponse) obj);
        }
    }

    private void execute(PermissionRequestResponse obj) {
        Class clazz = obj.getClass();
        Method[] methods = clazz.getMethods();

        if (clazz.isAnnotationPresent(HasRuntimePermissions.class))
            for (Method method : methods) {
                if (method.isAnnotationPresent(NeedPermissions.class)) {
                    String permission = method.getAnnotation(NeedPermissions.class).value();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        obj.permissionAllowed(); return;
                    }

                    if (act.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        obj.permissionDenied(); return;
                    } else if (act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                        obj.permissionAllowed(); return;
                    }

                    act.requestPermissions(new String[]{permission}, CODE);
                }
            }
    }

}
