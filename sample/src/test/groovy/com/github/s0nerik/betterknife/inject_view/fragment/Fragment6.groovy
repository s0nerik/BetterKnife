package com.github.s0nerik.betterknife.inject_view.fragment

import android.support.v4.app.Fragment
import android.widget.GridView
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnItemClick
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.fragment_test, injectAllViews = true)
class Fragment6 extends Fragment {
    TextView textViewLower
    TextView textViewCamel
    GridView grid

    @OnItemClick(R.id.grid)
    void onGridItemClicked(int pos) {

    }
}