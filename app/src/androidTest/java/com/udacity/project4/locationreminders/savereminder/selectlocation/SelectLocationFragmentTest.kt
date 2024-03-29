package com.udacity.project4.locationreminders.savereminder.selectlocation

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.CoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SelectLocationFragmentTest : AutoCloseKoinTest() {

    @get: Rule
    val mainCoroutineRule = CoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            val myModule = module {
                viewModel { SaveReminderViewModel(appContext, get() as ReminderDataSource) }
                single { RemindersLocalRepository(get()) as ReminderDataSource }
                single { RemindersLocalRepository(get()) }
                single { LocalDB.createRemindersDao(appContext) }
            }
            startKoin {
                modules(listOf(myModule))
            }
        }

    }

    @After
    fun cleanupDb() = runBlocking {
        stopKoin()
    }

    @Test
    fun selectLocation_UIdisplayed() {
        launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.save_location)).check(matches(isDisplayed())).check(matches(withText(appContext.getString(R.string.save))))
    }
}