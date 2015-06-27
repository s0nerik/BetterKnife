package com.github.s0nerik.betterknife.inject_view_activities.inheritance

import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class ChildActivity extends ParentActivity {

    @InjectView(R.id.btn_two)
    Button btnTwo

}