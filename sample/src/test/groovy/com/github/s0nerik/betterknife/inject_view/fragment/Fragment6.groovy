package com.github.s0nerik.betterknife.inject_view.fragment
import android.support.v4.app.Fragment
import android.widget.Button
import android.widget.GridView
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectViews
import com.github.s0nerik.betterknife.annotations.OnItemClick
import com.github.s0nerik.betterknife.annotations.testing.TestInvoke
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.layout_test, injectAllViews = true)
class Fragment6 extends Fragment {
    GridView grid

    @InjectViews([R.id.btn_one, R.id.btnTwo])
    List<Button> btnList

    @TestInvoke
    @OnItemClick(R.id.grid)
    void onGridItemClicked(int pos) {}
}