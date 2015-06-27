package com.github.s0nerik.betterknife.inject_view.activity
import android.app.Activity
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(R.layout.activity_test)
class Activity2 extends Activity {

    @InjectView(R.id.first)
    Button first
    @InjectView(R.id.second)
    Button second

}
