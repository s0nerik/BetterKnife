package com.github.s0nerik.betterknife.dsl.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.s0nerik.betterknife.dsl.AndroidDSL
import com.github.s0nerik.betterknife.dsl.AndroidEventDSL
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * A Form component that attaches entity properties
 * to view components
 * @param < T >
 *
 * @author Eugene Kamenev eugenekamenev
 */
@CompileStatic
@InheritConstructors
@SuppressLint('NewApi') // suppress lint warnings for constructors that are inherited.
class Form<T> extends LinearLayout {

    T object

    void build(T object, Closure content = null) {
        this.object = object
        def clone = content?.rehydrate(this, content?.owner, content?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        clone?.call(this)
        AndroidDSL.getChildren(this, true).findAll {
            it.tag != null && it instanceof TextView
        }.each { view ->
            ((TextView) view).setText((String) object[(String) view.tag])
        }
    }

    void submit(int id, @ClosureParams(value = FromString, options = 'T') Closure clickClosure) {
        this.submit(AndroidDSL.view(this, id), clickClosure)
    }

    void submit(View submitView,
                @ClosureParams(value = FromString, options = 'T') Closure clickClosure) {
        AndroidEventDSL.onClick(submitView) {
            def foundViews = AndroidDSL.getChildren(this, true).findAll {
                it.tag != null && it instanceof TextView
            }
            try {
                for (view in foundViews) {
                    def type = object.metaClass.properties.find {
                        it.name == (String) view.tag
                    }.type
                    switch (type) {
                        case String: object[(String) view.tag] = ((TextView) view).getText(); break;
                        case Number: object[(String) view.tag] = ((TextView) view).getText()
                                .toInteger(); break;
                    }
                }
            }
            catch (Exception e) {
                Log.e('ERROR', 'Form onSubmit caused: ', e)
            }
            clickClosure.call(object)
        }
    }
}
