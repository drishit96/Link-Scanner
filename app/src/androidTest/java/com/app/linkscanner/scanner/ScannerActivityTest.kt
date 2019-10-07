package com.app.linkscanner.scanner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.RunWith
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.app.linkscanner.R
import kotlinx.android.synthetic.main.activity_scanner.view.*
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.*


@RunWith(AndroidJUnit4::class)
class ScannerActivityTest {
//    private val idlingResource = CountingIdlingResource("asyncCall")
//
//    @Before
//    fun setup() {
//        IdlingRegistry.getInstance().register(idlingResource)
//    }
//
//    @After
//    fun finishingStep() {
//        IdlingRegistry.getInstance().unregister(idlingResource)
//    }

    @get:Rule
    val scannerActivityRule = ActivityTestRule<ScannerActivity>(ScannerActivity::class.java, false, false)

    @Test
    fun checkIfBasicDetailsIsFilledWhenAPIReturnData() {
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, ScannerActivity::class.java)
        intent.data = Uri.parse("https://bit.ly/1bhjUN8")
        scannerActivityRule.launchActivity(intent)

        onView(withId(R.id.scanning_in_progress)).check(matches(isDisplayed()))
        Thread.sleep(5000)
        onView(withId(R.id.scanning_in_progress)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cv_basic_details)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_domain)).check(matches(withText("duckduckgo.com")))
        onView(withId(R.id.tv_ip_address)).check(matches(withText(not(isEmptyOrNullString()))))
        onView(withId(R.id.cv_scan_results)).check(matches(isDisplayed()))
    }
}