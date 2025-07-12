package com.na982.icandoenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testOpenSideMenu() {
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.sideMenu)).check(matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
    }

    @Test
    fun testCategoryMenuClick() {
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withText("단어 카테고리")).perform(click())
        // 설정 화면으로 이동했는지 확인 (간단히 타이틀 텍스트 등으로 체크)
    }

    @Test
    fun testCalendarMenuClick() {
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withText("일정 관리")).perform(click())
        // 달력 화면으로 이동했는지 확인 (타이틀 텍스트 등으로 체크)
    }
} 