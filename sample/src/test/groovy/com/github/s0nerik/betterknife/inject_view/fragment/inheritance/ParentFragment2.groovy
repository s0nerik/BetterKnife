package com.github.s0nerik.betterknife.inject_view.fragment.inheritance

import android.support.v4.app.Fragment
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.layout_test, injectAllViews = true)
class ParentFragment2 extends Fragment {

    Button btnOne
    Button btnTwo

}