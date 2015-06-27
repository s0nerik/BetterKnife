package com.github.s0nerik.betterknife

import com.andrewreitz.spock.android.AndroidSpecification
import com.andrewreitz.spock.android.UseActivity
import com.github.s0nerik.betterknife.inject_view_activities.inheritance.ChildActivity

class ViewInjectionInheritanceTest extends AndroidSpecification {

    @UseActivity(ChildActivity)
    ChildActivity activity

    def "test view injection into activity"() {
        expect:
        activity.btnOne
        activity.btnTwo
    }

}