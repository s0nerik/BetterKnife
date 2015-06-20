package com.github.s0nerik.betterknife.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import groovy.transform.CompileStatic;

/**
 * Created by Arasthel on 16/08/14.
 */
@CompileStatic
class Finder {

    static final String TAG = "Finder";
    static final String ERROR_MSG = "Passed target is null, couldn't inject from it";

    static View findView(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG);
            return null;
        }

        if(target instanceof View) {
            Context context = target.getContext()
            int identifier = context.getResources().getIdentifier(idStr, "id", context.packageName)
            return target.findViewById(identifier)
        } else {
            Activity activity = target as Activity
            int identifier = activity.getResources().getIdentifier(idStr, "id", activity.packageName)
            return activity.findViewById(identifier)
        }
    }

    static String getString(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "string", context.packageName)
        return resources.getString(identifier)
    }

    static Float getDimen(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "dimen", context.packageName)
        return resources.getDimension(identifier)
    }

    static Boolean getBoolean(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "bool", context.packageName)
        return resources.getBoolean(identifier)
    }

    static Integer getColor(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "color", context.packageName)
        return resources.getColor(identifier)
    }

    static Integer getInteger(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "integer", context.packageName)
        return resources.getInteger(identifier)
    }

    static int[] getIntegerArray(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "array", context.packageName)
        return resources.getIntArray(identifier)
    }

    static String[] getStringArray(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "array", context.packageName)
        return resources.getStringArray(identifier)
    }

    static Drawable getDrawable(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "drawable", context.packageName)

        int api = android.os.Build.VERSION.SDK_INT

        return api >= 21 ? resources.getDrawable(identifier, context.getTheme()) : resources.getDrawable(identifier)
    }

    static Animation getAnimation(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        int identifier = context.getResources().getIdentifier(idStr, "anim", context.packageName)
        return AnimationUtils.loadAnimation(context, identifier)
    }

    static ColorStateList getColorStateList(Object target, String idStr) {
        if (!target) {
            Log.e(TAG, ERROR_MSG)
            return null
        }

        Context context = (target instanceof Activity) ?
                (target as Activity) : (target as View).getContext()

        Resources resources = context.getResources()
        int identifier = resources.getIdentifier(idStr, "color", context.packageName)
        return resources.getColorStateList(identifier)
    }
}
