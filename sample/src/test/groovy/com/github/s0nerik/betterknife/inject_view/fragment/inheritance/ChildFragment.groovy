package com.github.s0nerik.betterknife.inject_view.fragment.inheritance

import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class ChildFragment extends ParentFragment {

    @InjectView(R.id.tv2)
    TextView tv2

}