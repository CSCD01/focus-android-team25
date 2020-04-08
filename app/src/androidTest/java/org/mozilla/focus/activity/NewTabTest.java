/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.RadioButton;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.R;
import org.mozilla.focus.ext.ContextKt;
import org.mozilla.focus.helpers.SessionLoadedIdlingResource;
import org.mozilla.focus.helpers.TestHelper;
import org.mozilla.focus.utils.AppConstants;
import org.mozilla.focus.web.IWebView;

import mozilla.components.browser.session.SessionManager;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mozilla.focus.fragment.FirstrunFragment.FIRSTRUN_PREF;
import static org.mozilla.focus.helpers.EspressoHelper.*;
import static org.mozilla.focus.helpers.TestHelper.browserURLbar;
import static org.mozilla.focus.helpers.TestHelper.createMockResponseFromAsset;
import static org.mozilla.focus.helpers.TestHelper.pressEnterKey;
import static org.mozilla.focus.helpers.TestHelper.waitForIdle;
import static org.mozilla.focus.helpers.TestHelper.waitingTime;
import static org.mozilla.focus.helpers.WebViewFakeLongPress.injectHitTarget;

/**
 * Open new tab and verify that the UI looks like it should.
 */
@RunWith(AndroidJUnit4.class)
public class NewTabTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule
            = new ActivityTestRule<MainActivity>(MainActivity.class) {

        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            Context appContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext();

            PreferenceManager.getDefaultSharedPreferences(appContext)
                    .edit()
                    .putBoolean(FIRSTRUN_PREF, true)
                    .apply();
            TestHelper.selectGeckoForKlar();
        }
    };

    public String defaultEngine;
    @Before
    public void initDefaultBrowser() throws Exception {
        // Open Settings  and select Search submenu
        UiObject searchHeading = TestHelper.mDevice.findObject(new UiSelector()
                .text("Search")
                .resourceId("android:id/title"));
        assertTrue(TestHelper.inlineAutocompleteEditText.waitForExists(waitingTime));
        openSettings();
        TestHelper.settingsHeading.waitForExists(waitingTime);
        searchHeading.waitForExists(waitingTime);
        searchHeading.click();

        // Select Search engine
        UiObject searchEngineHeading = TestHelper.settingsMenu.getChild(new UiSelector()
                .className("android.widget.LinearLayout")
                .instance(0));
        searchEngineHeading.waitForExists(waitingTime);
        searchEngineHeading.click();

        // Set default search engine to Google
        defaultEngine = "Google";
        UiScrollable searchEngineList = new UiScrollable(new UiSelector()
                .resourceId(TestHelper.getAppName() + ":id/search_engine_group").enabled(true));
        UiObject defaultEngineSelection = searchEngineList.getChildByText(new UiSelector()
                .className(RadioButton.class), defaultEngine);
        defaultEngineSelection.waitForExists(waitingTime);
        assertTrue(defaultEngineSelection.getText().equals(defaultEngine));
        defaultEngineSelection.click();
        TestHelper.pressBackKey();
        TestHelper.pressBackKey();
        TestHelper.pressBackKey();
        TestHelper.inlineAutocompleteEditText.waitForExists(waitingTime);
    }

    @Test
    public void NewTabTest() throws UiObjectNotFoundException {
        /* Open initial tab to whats new menu item */
        openMenu();
        onView(withId(R.id.whats_new))
                .check(matches(isDisplayed()))
                .perform(click());
        assertTrue(TestHelper.browserURLbar.getText().contains("support.mozilla.org"));

        /* Check that the new tab option is present */
        openMenu();
        UiObject newTabOption = TestHelper.mDevice.findObject(new UiSelector()
                .text("Open New Tab")
                .resourceId(TestHelper.getAppName() + ":id/open_new_tab"));
        newTabOption.waitForExists(waitingTime);
        assertTrue(newTabOption.exists());

        /* Check that the new tab is opened correctly */
        newTabOption.click();
        assertTrue(TestHelper.browserURLbar.getText().contains("google.com"));
    }
}
