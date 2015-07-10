package com.github.s0nerik.betterknife.inject_view.fragment.inheritance

import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.annotations.testing.TestInvoke
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class ChildFragment2 extends ParentFragment2 {

    @TestInvoke
    @OnClick([R.id.btn_one, R.id.btnTwo])
    void anyBtnClicked() {}

}