package com.github.s0nerik.betterknife.inject_view.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class Activity1 extends Activity {

    @InjectView(R.id.first)
    Button first
    @InjectView(R.id.second)
    Button second

    boolean testFlag = false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        contentView = R.layout.activity_test
        testFlag = true
    }
}
