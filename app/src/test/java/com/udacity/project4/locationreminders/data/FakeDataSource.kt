package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var returnError = false

    override suspend fun getReminders(userEmail : String): Result<List<ReminderDTO>> {
        if (returnError) {
            return Result.Error("Error or exception")
        }
        remindersList?.let { return Result.Success(it) }
        return Result.Error("No reminder found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO){
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (returnError) {
            return Result.Error("Error or exception")
        }
        remindersList?.firstOrNull() { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }

    fun setReturnError(value: Boolean) {
        returnError = value
    }

}