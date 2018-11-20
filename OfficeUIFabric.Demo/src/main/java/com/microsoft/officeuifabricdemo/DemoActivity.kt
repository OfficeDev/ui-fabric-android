package com.microsoft.officeuifabricdemo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.jakewharton.threetenabp.AndroidThreeTen
import com.microsoft.officeuifabric.datepicker.DateTimePickerListener
import com.microsoft.officeuifabricdemo.demos.DateTimePickerDialogFragment
import kotlinx.android.synthetic.main.activity_demo_detail.*
import org.threeten.bp.ZonedDateTime
import java.util.*

class DemoActivity : AppCompatActivity(), DateTimePickerListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_demo_detail)
        setSupportActionBar(detail_toolbar)

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            val demoID = intent.getSerializableExtra(DemoFragment.DEMO_ID) as UUID
            val demo = DEMOS.find { it.id == demoID }
            if (demo == null)
                return

            val fragment = Fragment.instantiate(this, demo.demoClass.java.name).apply {
                arguments = Bundle().apply {
                    putSerializable(DemoFragment.DEMO_ID, demoID)
                }
            }

            val isDemoScrollable = (fragment as? DemoFragment)?.needsScrollableContainer() ?: true

            supportFragmentManager.beginTransaction()
                .add(if (isDemoScrollable) R.id.demo_detail_scrollable_container else R.id.demo_detail_container, fragment, demo.title)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                    navigateUpTo(Intent(this, DemoListActivity::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onDatePicked(date: ZonedDateTime) {
        val dateTimePickerDialogFragment = supportFragmentManager.findFragmentByTag(DATE_TIME_PICKER_DIALOG) as? DateTimePickerDialogFragment
        dateTimePickerDialogFragment?.date = date
    }
}
