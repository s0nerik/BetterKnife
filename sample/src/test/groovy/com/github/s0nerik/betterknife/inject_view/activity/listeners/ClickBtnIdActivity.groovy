package com.github.s0nerik.betterknife.inject_view.activity.listeners
import android.app.Activity
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.activity_test, injectAllViews = true)
class ClickBtnIdActivity extends Activity {

    boolean isFirstClicked = false
    boolean isAnyClicked = false

    @OnClick(R.id.first)
    public void onFirstClicked() {
        isFirstClicked = true
    }

    @OnClick([R.id.second, R.id.third])
    public void onAnyClicked() {
        isFirstClicked = true
    }

}