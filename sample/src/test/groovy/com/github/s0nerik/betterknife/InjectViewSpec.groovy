package com.github.s0nerik.betterknife

import com.github.s0nerik.betterknife.inject_view_activities.Activity1
import com.github.s0nerik.betterknife.inject_view_activities.Activity2
import com.github.s0nerik.betterknife.inject_view_activities.Activity3
import com.github.s0nerik.betterknife.inject_view_activities.inheritance.ChildActivity
import com.github.s0nerik.betterknife.util.SampleSpecification
import org.robolectric.Robolectric

class InjectViewSpec extends SampleSpecification {

    def "should inject parent view and it's own"() {
        given:
        def childActivity = Robolectric.buildActivity(ChildActivity).create().get()

        expect:
        childActivity.btnTwo
        childActivity.btnOne
    }

    def "injecting views into activity without @InjectLayout and onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity1).create().get()

        expect:
        activity.first
        activity.second
    }

    def "injecting views into activity with @InjectLayout and no onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity2).create().get()

        expect:
        activity.first
        activity.second
    }

    def "injecting views into activity with @InjectLayout and onCreate() defined"() {
        given:
        def activity = Robolectric.buildActivity(Activity3).create().get()

        expect:
        activity.first
        activity.second
        activity.testFlag
    }

}