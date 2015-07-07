package com.github.s0nerik.betterknife.inject_view.object

import android.view.View
import android.widget.Button
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class Object1 {

    @InjectView(R.id.btn_one)
    Button btnOne

    @InjectView
    Button btnTwo

    Object1(View v) {
        BetterKnife.inject(v)
    }
}