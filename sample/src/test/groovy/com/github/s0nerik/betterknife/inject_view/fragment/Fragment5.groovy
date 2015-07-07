package com.github.s0nerik.betterknife.inject_view.fragment
import android.support.v4.app.Fragment
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.annotations.testing.TestInvoke
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.fragment_test, injectAllViews = true)
class Fragment5 extends Fragment {
    TextView textViewLower
    TextView textViewCamel

    boolean textViewClicked = false

    @TestInvoke
    @OnClick(R.id.text_view_lower)
    void onTextViewLowerClicked() {
        textViewClicked = true
    }
}