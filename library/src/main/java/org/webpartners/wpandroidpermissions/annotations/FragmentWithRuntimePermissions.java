package org.webpartners.wpandroidpermissions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Jorge Garrido Oval on 18/08/15.
 *
 * At class level for Fragments
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface FragmentWithRuntimePermissions {

}