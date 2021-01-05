package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.CoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeRemindersDb() {
        database.close()
    }

    @Test
    fun save_get_reminder() = runBlockingTest {
        remindersRepository.deleteAllReminders()

        val reminder = ReminderDTO("title", "description","location",2.0, 1.0, "xyz@gmail.com")
        remindersRepository.saveReminder(reminder)

        val result = remindersRepository.getReminder(reminder.id) as Result.Success<ReminderDTO>

        Assert.assertThat(result.data.id, CoreMatchers.`is`(reminder.id))
        Assert.assertThat(result.data.title, CoreMatchers.`is`(reminder.title))
        Assert.assertThat(result.data.description, CoreMatchers.`is`(reminder.description))
        Assert.assertThat(result.data.location, CoreMatchers.`is`(reminder.location))
        Assert.assertThat(result.data.latitude, CoreMatchers.`is`(reminder.latitude))
        Assert.assertThat(result.data.longitude, CoreMatchers.`is`(reminder.longitude))
        Assert.assertThat(result.data.userEmail, CoreMatchers.`is`(reminder.userEmail))
    }

    @Test
    fun save_get_all_reminders() = runBlockingTest {
        remindersRepository.deleteAllReminders()

        val remindersList = mutableListOf<ReminderDTO>(
                ReminderDTO("title", "description", "location", 0.0, 0.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 8.0, 4.0, "xyz@gmail.com")
        )
        remindersList.forEach {
            remindersRepository.saveReminder(it)
        }

        val reminders = remindersRepository.getReminders("xyz@gmail.com") as Result.Success<List<ReminderDTO>>

        Assert.assertThat(reminders.data.size, CoreMatchers.`is`(2))
        Assert.assertThat(reminders.data, CoreMatchers.notNullValue())
        Assert.assertThat(reminders.data, CoreMatchers.hasItem(remindersList[0]))
        Assert.assertThat(reminders.data, CoreMatchers.hasItem(remindersList[1]))

        Assert.assertThat(reminders.data[0].id, CoreMatchers.`is`(remindersList[0].id))
        Assert.assertThat(reminders.data[0].title, CoreMatchers.`is`(remindersList[0].title))
        Assert.assertThat(reminders.data[0].description, CoreMatchers.`is`(remindersList[0].description))
        Assert.assertThat(reminders.data[0].location, CoreMatchers.`is`(remindersList[0].location))
        Assert.assertThat(reminders.data[0].latitude, CoreMatchers.`is`(remindersList[0].latitude))
        Assert.assertThat(reminders.data[0].longitude, CoreMatchers.`is`(remindersList[0].longitude))
        Assert.assertThat(reminders.data[0].userEmail, CoreMatchers.`is`(remindersList[0].userEmail))

        Assert.assertThat(reminders.data[1].id, CoreMatchers.`is`(remindersList[1].id))
        Assert.assertThat(reminders.data[1].title, CoreMatchers.`is`(remindersList[1].title))
        Assert.assertThat(reminders.data[1].description, CoreMatchers.`is`(remindersList[1].description))
        Assert.assertThat(reminders.data[1].location, CoreMatchers.`is`(remindersList[1].location))
        Assert.assertThat(reminders.data[1].latitude, CoreMatchers.`is`(remindersList[1].latitude))
        Assert.assertThat(reminders.data[1].longitude, CoreMatchers.`is`(remindersList[1].longitude))
        Assert.assertThat(reminders.data[1].userEmail, CoreMatchers.`is`(remindersList[1].userEmail))

    }

    @Test
    fun save_deleteAll_get_reminders() = runBlockingTest {

        val reminders = mutableListOf<ReminderDTO>(
                ReminderDTO("title", "description", "location", 0.0, 0.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 2.0, 1.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 4.0, 2.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 6.0, 3.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 8.0, 4.0, "xyz@gmail.com")
        )
        reminders.forEach {
            remindersRepository.saveReminder(it)
        }

        val reminderList = remindersRepository.getReminders("xyz@gmail.com") as Result.Success<List<ReminderDTO>>

        MatcherAssert.assertThat(reminderList.data, notNullValue())
        MatcherAssert.assertThat(reminderList.data.size, `is`(5))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminders[0]))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminders[1]))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminders[2]))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminders[3]))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminders[4]))

        remindersRepository.deleteAllReminders()

        val reminders_list = remindersRepository.getReminders("xyz@gmail.com") as Result.Success<List<ReminderDTO>>
        MatcherAssert.assertThat(reminders_list.data, notNullValue())
        MatcherAssert.assertThat(reminders_list.data.size, `is`(0))
    }

}