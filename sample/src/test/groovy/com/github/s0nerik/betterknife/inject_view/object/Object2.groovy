package com.github.s0nerik.betterknife.inject_view.object

import android.view.View
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class Object2 extends Object1 {

    @InjectView
    TextView textViewOne

    boolean btnOneClicked = false

    Object2(View v) {
        super(v)
    }

    @OnClick(R.id.btn_one)
    void onBtnOneClicked() {
        btnOneClicked = true
    }
}