package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.CoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsNot
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    @Before
    fun setupViewModel() = runBlockingTest {
        val reminders = mutableListOf<ReminderDTO>(
                ReminderDTO("title", "description", "location", 0.0, 0.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 2.0, 1.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 4.0, 2.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 6.0, 3.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 8.0, 4.0, "xyz@gmail.com")
        )
        fakeDataSource = FakeDataSource(reminders)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun cleanupDataSource() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun getRemindersList() {
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.remindersList.getOrAwaitValue(), (IsNot.not(emptyList())))
        Assert.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()?.size, CoreMatchers.`is`(5))
    }

    @Test
    fun getZeroReminders() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.remindersList.getOrAwaitValue()?.size, CoreMatchers.`is`(0))
    }

    @Test
    fun check_loading() {
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun shouldReturnError() {
        fakeDataSource.setReturnError(true)
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`("Error or exception"))
    }

    @Test
    fun testInvalidateShowNoData() =  runBlockingTest{
        fakeDataSource.deleteAllReminders()
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.showNoData.getOrAwaitValue().toString(), CoreMatchers.`is`("true"))
    }

}