package com.github.s0nerik.betterknife.inject_view.object
import android.view.View
import android.widget.Button
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.sample.R
import groovy.transform.CompileStatic

@CompileStatic
class Object3 extends Object1 {

    boolean btnOneClicked = false

    Nested2 nested2

    Object3(View v) {
        super(v)
        nested2 = new Nested2(v)
    }

    abstract class Nested1 {

        @InjectView(R.id.btn_one)
        Button btnOne

        @InjectView
        Button btnTwo

        Nested1(View v) {
            BetterKnife.inject(v)
        }

    }

    class Nested2 extends Nested1 {

        Nested2(View view) {
            super(view);
        }

        @OnClick(R.id.btn_one)
        void onBtnOnelicked() {
            btnOneClicked = true
        }

    }

    class Nested3 extends Nested1 {

        Nested3(View view) {
            super(view);
        }

//        @OnClick(R.id.btn_two)
//        void onBtnTwoClicked() {
//        }

    }
}