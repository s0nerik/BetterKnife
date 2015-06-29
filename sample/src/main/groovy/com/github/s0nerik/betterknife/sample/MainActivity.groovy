package com.github.s0nerik.betterknife.sample
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.Profile
import com.github.s0nerik.betterknife.dsl.components.GAsyncTask
import groovy.transform.CompileStatic

import static com.github.s0nerik.betterknife.dsl.AndroidDSL.async
import static com.github.s0nerik.betterknife.dsl.AndroidDSL.toast

@CompileStatic
public class MainActivity extends AppCompatActivity {

    private Context mContext

    @InjectView(R.id.first_textview)
    TextView firstTextView
    // tag::injectViewWithId[]
    @InjectView(R.id.first_button)
    Button firstButton
    // end::injectViewWithId[]
    @InjectView(R.id.list_view)
    ListView listView
    @InjectView(R.id.written_text)
    TextView writtenTextView

//    //for now only R.* resources are supported, so you can't use android.R.* on resource annotations
//    @StringRes(R.string.app_name)
//    String title
//
//    // tag::methodAwareAnnotation[]
//    @OnTextChanged(value = R.id.edit_text, method = OnTextChanged.Method.ON_TEXT_CHANGED)
//    @Profile
//    public void onTextChanged(CharSequence sequence) {
//        writtenTextView.text = sequence
//    }
//    // end::methodAwareAnnotation[]
//
//    @OnEditorAction(R.id.edit_text)
//    @Profile
//    public boolean onEditorAction(KeyEvent key) {
//        toast this, 'Editor action received' show()
//        true
//    }
//
//    // tag::onClick[]
//    @OnClick(R.id.first_button)
//    @Profile
//    public void clicked() {
//        firstButton.text = 'I\'ve been clicked! Click me longer!'
//        profileMethod('param1Value', 'param2Value')
//    }
//    // end::onClick[]
//
//    @OnLongClick(R.id.first_button)
//    public boolean longClicked() {
//        firstButton.text = 'I\'ve been clicked for a long time!'
//        profileMethod('anotherValue1', 'anotherValue2', 5)
//        return true
//    }
//
//
//    @OnClick(R.id.second_button)
//    public void changeText() {
//        firstTextView.text = 'You have pressed the second button!'
//    }
//
//    @OnClick(R.id.new_activity)
//    @Profile
//    public void newActivity() {
//        startActivity new Intent(mContext, BackgroundActivity.class)
//    }
//
//    @OnItemClick(R.id.list_view)
//    @Profile
//    public void onItemClick(int position) {
//        toast this, "Pressed item number $position" show()
//    }
//
//    @OnItemLongClick(R.id.list_view)
//    @Profile
//    public boolean onItemLongClick(int position) {
//        toast this, "Long pressed item number $position" show()
//        true
//    }
//
//    // tag::multipleOnClick[]
//    @OnClick([R.id.third_button, R.id.fourth_button])
//    public void onClick() {
//        toast this, 'Button three or four has been clicked' show()
//    }
//    // end::multipleOnClick[]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        mContext = this
        contentView = R.layout.activity_main
//        BetterKnife.inject this


        firstTextView.text = "$title <-- this thing was injected from strings.xml"

        getSupportActionBar()?.title = title

        def items = generateItems()
        listView.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
    }

    @Profile
    List<String> generateItems() {
        def strings = []
        20.times {
            strings << "Element $it"
        }
        return strings
    }

    @Profile
    def profileMethod(String param1, String param2, int f = 4) {
        async this, { MainActivity context, GAsyncTask task ->
            task.after {
                toast context, 'Hey! Async task just finished' show()
            }
            task.error { e ->
                toast context, 'WTF. Error raised.' show()
            }
            Thread.sleep(10000)
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu)
        true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true
        }
        super.onOptionsItemSelected(item)
    }
}
