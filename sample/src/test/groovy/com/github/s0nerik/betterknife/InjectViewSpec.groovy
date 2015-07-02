package com.github.s0nerik.betterknife

import com.github.s0nerik.betterknife.inject_view.activity.*
import com.github.s0nerik.betterknife.inject_view.activity.inheritance.ChildActivity
import com.github.s0nerik.betterknife.inject_view.fragment.Fragment1
import com.github.s0nerik.betterknife.inject_view.fragment.Fragment2
import com.github.s0nerik.betterknife.inject_view.fragment.Fragment3
import com.github.s0nerik.betterknife.inject_view.fragment.Fragment4
import com.github.s0nerik.betterknife.inject_view.fragment.inheritance.ChildFragment
import com.github.s0nerik.betterknife.util.SampleSpecification
import org.robolectric.Robolectric
import org.robolectric.util.FragmentTestUtil

class InjectViewSpec extends SampleSpecification {

    def "injecting views into activity inherited from base activity"() {
        given:
        def childActivity = Robolectric.buildActivity(ChildActivity).create().get()

        expect:
        childActivity.btnOne && childActivity.btnTwo
    }

    def "injecting views into activity without @InjectLayout and onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity1).create().get()

        expect:
        activity.first && activity.second
    }

    def "injecting views into activity with @InjectLayout and no onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity2).create().get()

        expect:
        activity.first && activity.second
    }

    def "injecting views into activity with @InjectLayout and onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity3).create().get()

        expect:
        activity.first && activity.second && activity.testFlag
    }

    def "injecting all views into activity with @InjectLayout(injectAllViews = true)"() {
        given:
        def activity = Robolectric.buildActivity(Activity5).create().get()

        expect:
        (activity."$field" as boolean) == exists

        where:
        field    || exists
        'first'  || true
        'second' || true
        'third'  || true
        'fourth' || false
    }

    def "listeners injection"() {
        given:
        def activity = Robolectric.buildActivity(Activity4).create().resume().start().visible().get()
        def btn1 = Robolectric.shadowOf(activity.first)
        def btn2 = Robolectric.shadowOf(activity.second)

        expect:
        btn1.onClickListener
        btn2.onClickListener

        when:
        btn1.checkedPerformClick()

        then:
        activity.isFirstOrSecondClicked
    }

    def "injecting views into fragment without @InjectLayout and no onViewCreated() defined"() {
        given:
        def fragment = new Fragment1()

        when:
        FragmentTestUtil.startFragment(fragment)

        then:
        fragment.tv1
    }

    def "injecting views into fragment with @InjectLayout and no onViewCreated() defined"() {
        given:
        def fragment = new Fragment2()

        when:
        FragmentTestUtil.startFragment(fragment)

        then:
        fragment.tv1
    }

    def "injecting views into fragment with @InjectLayout and onViewCreated() defined"() {
        given:
        def fragment = new Fragment3()

        when:
        FragmentTestUtil.startFragment(fragment)

        then:
        fragment.tv1 && fragment.testFlag
    }

    def "injecting views with implicit id"() {
        given:
        def fragment = new Fragment4()

        when:
        FragmentTestUtil.startFragment(fragment)

        then:
        fragment.textViewCamel && fragment.textViewLower
    }

    def "injecting views into fragment inherited from base fragment"() {
        given:
        def fragment = new ChildFragment()

        when:
        FragmentTestUtil.startFragment(fragment)

        then:
        fragment.tv1 && fragment.tv2
    }

}