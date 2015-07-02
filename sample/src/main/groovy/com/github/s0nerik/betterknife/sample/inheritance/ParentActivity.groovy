package com.github.s0nerik.betterknife.sample.inheritance
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@InjectLayout(R.layout.activity_inheritance)
@CompileStatic
class ParentActivity extends AppCompatActivity {

//    @InjectView(R.id.btn_one)
    Button btnOne
    Button btnThree

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        this._injectViews(this.findViewById(android.R.id.content))
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
    }

    void _injectViews(View v) {
        btnOne = v.findViewById(R.id.btn_one) as Button
        btnThree = v.findViewById(R.id.btn_three) as Button
    }
}