package com.github.s0nerik.betterknife.sample.inheritance
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.View
import android.widget.Button
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class ChildActivity extends ParentActivity {

//    @InjectView(R.id.btn_two)
    Button btnTwo

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
    }

    @Override
    void _injectViews(View v) {
        super._injectViews(v)
        btnTwo = v.findViewById(R.id.btn_two) as Button
    }
}