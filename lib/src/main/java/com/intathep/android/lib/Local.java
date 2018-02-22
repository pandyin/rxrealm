package com.intathep.android.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pan on 11/10/16.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Local {

}
