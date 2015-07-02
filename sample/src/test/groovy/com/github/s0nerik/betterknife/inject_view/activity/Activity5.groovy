package com.github.s0nerik.betterknife.inject_view.activity
import android.app.Activity
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.activity_test, injectAllViews = true)
class Activity5 extends Activity {

    Button first
    Button second
    Button third
    Button fourth

}
