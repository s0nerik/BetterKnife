package com.github.s0nerik.betterknife.inject_view.fragment

import android.support.v4.app.Fragment
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@InjectLayout(R.layout.fragment_test)
@CompileStatic
class Fragment4 extends Fragment {

    @InjectView
    TextView textViewLower

    @InjectView
    TextView textViewCamel

}