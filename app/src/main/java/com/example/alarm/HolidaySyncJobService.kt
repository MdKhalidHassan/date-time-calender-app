package com.example.alarm

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.example.DateTimeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HolidaySyncJobService : JobService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeJob: Job? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        val appContext = applicationContext as? DateTimeApplication ?: run {
            jobFinished(params, false)
            return false
        }

        val prefs = getSharedPreferences("datetime_prefs", Context.MODE_PRIVATE)
        val countryCode = prefs.getString("pref_holiday_country_code", null)
        if (countryCode.isNullOrBlank()) {
            jobFinished(params, false)
            return false
        }

        val countryName = prefs.getString("pref_holiday_country_name", countryCode) ?: countryCode
        val remindersEnabled = prefs.getBoolean("pref_holiday_reminders_enabled", false)
        val daysBefore = prefs.getInt("pref_holiday_remind_days_before", 1).coerceIn(0, 30)
        val hour = prefs.getInt("pref_holiday_remind_hour", 9).coerceIn(0, 23)
        val minute = prefs.getInt("pref_holiday_remind_minute", 0).coerceIn(0, 59)
        val scheduler = AlarmScheduler(applicationContext)

        activeJob = scope.launch {
            val result = appContext.holidayRepository.syncAndReconcileReminders(
                countryCode = countryCode,
                countryName = countryName,
                scheduler = scheduler,
                remindersEnabled = remindersEnabled,
                daysBefore = daysBefore,
                hour = hour,
                minute = minute
            )

            if (result.isSuccess) {
                prefs.edit()
                    .putLong("pref_holiday_last_sync_at", System.currentTimeMillis())
                    .putString("pref_holiday_last_sync_country", countryCode)
                    .apply()
                jobFinished(params, false)
            } else {
                // Retry later when network/API recovers
                jobFinished(params, true)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        activeJob?.cancel()
        return true
    }

    companion object {
        private const val JOB_ID = 7001
        private const val SYNC_INTERVAL_MS = 24L * 60L * 60L * 1000L

        fun schedulePeriodicSync(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
            if (jobScheduler.getPendingJob(JOB_ID) != null) return

            val component = ComponentName(context, HolidaySyncJobService::class.java)
            val jobInfo = JobInfo.Builder(JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(SYNC_INTERVAL_MS)
                .setPersisted(true)
                .build()

            try {
                jobScheduler.schedule(jobInfo)
            } catch (_: Exception) {
            }
        }
    }
}
