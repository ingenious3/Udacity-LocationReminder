package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.CoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.hamcrest.CoreMatchers
import org.koin.core.context.stopKoin
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.nullValue
import org.junit.*
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun cleanupDataSource() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun check_loading() {
        val reminder = ReminderDataItem("title", "description","location",2.0, 1.0, "xyz@gmail.com")
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        Assert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), CoreMatchers.`is`("Reminder Saved !"))
    }

    @Test
    fun shouldReturnError() = runBlockingTest {
        fakeDataSource.setReturnError(true)
        val reminder = ReminderDataItem("", "description","location",2.0, 1.0, "xyz@gmail.com")
        Assert.assertThat(saveReminderViewModel.validateEnteredData(reminder), CoreMatchers.`is`(false))
        Assert.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun testValidateEnteredData() {

        val reminder_1 = ReminderDataItem("", "","",null, null, "xyz@gmail.com")
        Assert.assertThat(saveReminderViewModel.validateEnteredData(reminder_1), CoreMatchers.`is`(false))

        val reminder_2 = ReminderDataItem("title", "description","location",2.0, 1.0, "xyz@gmail.com")
        Assert.assertThat(saveReminderViewModel.validateEnteredData(reminder_2), CoreMatchers.`is`(true))
    }



    @Test
    fun testOnClear() = mainCoroutineRule.runBlockingTest {
        saveReminderViewModel.onClear()

        Assert.assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), CoreMatchers.`is`(nullValue()))
        Assert.assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), CoreMatchers.`is`(nullValue()))
        Assert.assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), CoreMatchers.`is`(nullValue()))
        Assert.assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), CoreMatchers.`is`(nullValue()))
        Assert.assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), CoreMatchers.`is`(nullValue()))


    }

}