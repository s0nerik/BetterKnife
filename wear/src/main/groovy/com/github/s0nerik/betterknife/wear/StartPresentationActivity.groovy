package com.github.s0nerik.betterknife.wear

import android.app.Activity
import android.os.Bundle
import groovy.transform.CompileStatic

@CompileStatic
class StartPresentationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)
    }
}