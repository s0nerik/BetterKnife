package com.github.s0nerik.betterknife.sample.test

import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@InjectLayout(R.layout.activity_inheritance)
@CompileStatic
class ParentActivity extends AppCompatActivity {

    @InjectView(R.id.btn_one)
    Button btnOne

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState)
//        contentView = R.layout.activity_inheritance
//    }
}