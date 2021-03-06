package org.xdty.callerinfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xdty.callerinfo.activity.MainActivity;
import org.xdty.callerinfo.activity.SettingsActivity;
import org.xdty.callerinfo.model.setting.Setting;
import org.xdty.callerinfo.model.setting.SettingImpl;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class MainActivityTest {

    private static final String BASIC_PACKAGE = "org.xdty.callerinfo";

    private static final int LAUNCH_TIMEOUT = 5000;

    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
            MainActivity.class);
    private UiDevice mDevice;

    private Setting mSetting;

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    public static ViewAction swipeUp() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER);
    }

    public static ViewAction swipeDown() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER, Press.FINGER);
    }

    public static Matcher<View> isWindowAtPosition(final int x, final int y) {
        return new TypeSafeMatcher<View>() {

            int rootX = -1;
            int rootY = -1;

            @Override
            protected boolean matchesSafely(View view) {
                if (view.getRootView().getLayoutParams() instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getRootView()
                            .getLayoutParams();
                    rootX = params.x;
                    rootY = params.y;
                    return rootX == x && rootY == y;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("view is at position: (" + rootX + " ," + rootY
                        + "), but should at x=" + x + ", y=" + y + "");
            }
        };
    }

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the blueprint app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(BASIC_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_PACKAGE).depth(0)), LAUNCH_TIMEOUT);

        SettingImpl.init(getTargetContext());
        mSetting = SettingImpl.getInstance();
    }

    @Test
    public void testPreconditions() {
        assertThat(mDevice, notNullValue());
    }

    @Test
    public void testEmptyList() {
        UiObject2 list = mDevice.wait(Until.findObject(By.res(BASIC_PACKAGE, "history_list")), 500);
        UiObject2 empty = mDevice.findObject(By.res(BASIC_PACKAGE, "empty_text"));

        if (list == null) {
            assertThat(empty, notNullValue());
        }

        if (empty == null) {
            assertThat(list, notNullValue());
        }
    }

    @Test
    public void testActionSetting() {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.action_settings))
                .perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), SettingsActivity.class)));
        //pressBack();
    }

    @Test
    public void testRecyclerViewItemClick() {
        // Todo: may use mock data
        onView(withId(R.id.history_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0,
                clickChildViewWithId(R.id.card_view)));
        onView(allOf(withId(R.id.time),
                hasSibling(withText("17 秒")))).check(matches(isDisplayed()));
        onView(withId(R.id.history_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0,
                clickChildViewWithId(R.id.card_view)));
        onView(allOf(withId(R.id.time),
                hasSibling(withText("17 秒")))).check(matches(not(isDisplayed())));
    }

    @Test
    public void testActionSearch() {
        onView(withId(R.id.action_search)).perform(click());
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()));
        onView(withId(R.id.search_src_text)).check(matches(withHint(R.string.search_hint)));
        onView(isAssignableFrom(ImageButton.class)).perform(click());
        onView(withId(R.id.search_src_text)).check(doesNotExist());
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("10086"),
                pressKey(KeyEvent.KEYCODE_ENTER));

        onView(withId(R.id.window_layout)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
        onView(withId(R.id.number_info)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(withText("中国移动客服")));

        onView(withId(R.id.history_list)).check(matches(not(isDisplayed())));

        onView(isAssignableFrom(ImageButton.class)).perform(click());
        onView(withId(R.id.window_layout)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(doesNotExist());
        onView(withId(R.id.history_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testActionMoveWindowPosition() {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.action_float_window))
                .perform(click());
        onView(withId(R.id.window_layout)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
        onView(withId(R.id.number_info)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(withText(R.string.float_window_hint)));

        onView(withId(R.id.number_info)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .perform(swipeUp());
        onView(withId(R.id.window_layout)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isWindowAtPosition(mSetting.getWindowX(), mSetting.getWindowY())));

        onView(withId(R.id.number_info)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .perform(swipeDown());
        onView(withId(R.id.window_layout)).inRoot(
                withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isWindowAtPosition(mSetting.getWindowX(), mSetting.getWindowY())));
    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.
     */
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}
