package com.github.s0nerik.betterknife.inject_view.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class Fragment1 extends Fragment {

    @InjectView(R.id.tv1)
    TextView tv1

    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        def v = inflater.inflate(R.layout.fragment_test, container, false)
        return v
    }
}