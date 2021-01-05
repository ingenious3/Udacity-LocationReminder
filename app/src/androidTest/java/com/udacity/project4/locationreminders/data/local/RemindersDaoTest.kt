package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java)
                .build()
    }

    @After
    fun closeRemindersDb() {
        database.close()
    }


    @Test
    fun saveReminders_getReminders() = runBlockingTest {

        database.reminderDao().deleteAllReminders()

        val remindersList = mutableListOf<ReminderDTO>(
                ReminderDTO("title", "description", "location", 0.0, 0.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 8.0, 4.0, "xyz@gmail.com")
        )
        remindersList.forEach {
            database.reminderDao().saveReminder(it)
        }

        val reminders = database.reminderDao().getReminders("xyz@gmail.com")

        Assert.assertThat(reminders.size, `is`(2))
        Assert.assertThat(reminders, notNullValue())
        Assert.assertThat(reminders, hasItem(remindersList[0]))
        Assert.assertThat(reminders, hasItem(remindersList[1]))

        Assert.assertThat(reminders[0].id, `is`(remindersList[0].id))
        Assert.assertThat(reminders[0].title, `is`(remindersList[0].title))
        Assert.assertThat(reminders[0].description, `is`(remindersList[0].description))
        Assert.assertThat(reminders[0].location, `is`(remindersList[0].location))
        Assert.assertThat(reminders[0].latitude, `is`(remindersList[0].latitude))
        Assert.assertThat(reminders[0].longitude, `is`(remindersList[0].longitude))
        Assert.assertThat(reminders[0].userEmail, `is`(remindersList[0].userEmail))

        Assert.assertThat(reminders[1].id, `is`(remindersList[1].id))
        Assert.assertThat(reminders[1].title, `is`(remindersList[1].title))
        Assert.assertThat(reminders[1].description, `is`(remindersList[1].description))
        Assert.assertThat(reminders[1].location, `is`(remindersList[1].location))
        Assert.assertThat(reminders[1].latitude, `is`(remindersList[1].latitude))
        Assert.assertThat(reminders[1].longitude, `is`(remindersList[1].longitude))
        Assert.assertThat(reminders[1].userEmail, `is`(remindersList[1].userEmail))

    }

    @Test
    fun saveReminder_getReminderById() = runBlockingTest {

        database.reminderDao().deleteAllReminders()

        val reminder = ReminderDTO("title", "description","location",2.0, 1.0, "xyz@gmail.com")
        database.reminderDao().saveReminder(reminder)

        val reminderFromDB = database.reminderDao().getReminderById(reminder.id)

        Assert.assertThat<ReminderDTO>(reminderFromDB as ReminderDTO, notNullValue())
        Assert.assertThat(reminderFromDB.id, `is`(reminder.id))
        Assert.assertThat(reminderFromDB.title, `is`(reminder.title))
        Assert.assertThat(reminderFromDB.description, `is`(reminder.description))
        Assert.assertThat(reminderFromDB.location, `is`(reminder.location))
        Assert.assertThat(reminderFromDB.latitude, `is`(reminder.latitude))
        Assert.assertThat(reminderFromDB.longitude, `is`(reminder.longitude))
        Assert.assertThat(reminderFromDB.userEmail, `is`(reminder.userEmail))
    }

    @Test
    fun getReminderBy_notFound() = runBlockingTest {
//       save a reminder & get a reminder with random id -> reminder not found
        val reminder = ReminderDTO("title", "description","location",2.0, 1.0, "xyz@gmail.com")
        database.reminderDao().saveReminder(reminder)
        Assert.assertNull(database.reminderDao().getReminderById(UUID.randomUUID().toString()))

//      2nd attempt to  get a reminder with random id -> reminder not found
        Assert.assertNull(database.reminderDao().getReminderById(UUID.randomUUID().toString()))
    }


    @Test
    fun save_delete_get_reminders() = runBlockingTest {

        val reminders = mutableListOf<ReminderDTO>(
                ReminderDTO("title", "description", "location", 0.0, 0.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 2.0, 1.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 4.0, 2.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 6.0, 3.0, "xyz@gmail.com"),
                ReminderDTO("title", "description", "location", 8.0, 4.0, "xyz@gmail.com")
        )
        reminders.forEach {
            database.reminderDao().saveReminder(it)
        }

        database.reminderDao().deleteAllReminders()

        val remindersFromDB = database.reminderDao().getReminders("xyz@gmail.com")
        Assert.assertThat(remindersFromDB.isEmpty(), `is`(true))
    }

}