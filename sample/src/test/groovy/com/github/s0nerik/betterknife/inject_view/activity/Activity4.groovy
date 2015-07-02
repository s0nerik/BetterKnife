package com.github.s0nerik.betterknife.inject_view.activity
import android.app.Activity
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(R.layout.activity_test)
class Activity4 extends Activity {

    @InjectView(R.id.first)
    Button first
    @InjectView(R.id.second)
    Button second

    boolean isFirstClicked = false
    boolean isFirstOrSecondClicked = false

    @OnClick([R.id.first, R.id.second])
    void onFirstOrSecondClicked() {
        isFirstOrSecondClicked = true
    }
}
