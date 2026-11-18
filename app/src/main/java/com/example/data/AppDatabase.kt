package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AlarmDao
import com.example.data.dao.HolidayDao
import com.example.data.dao.NoteDao
import com.example.data.model.DateAlarm
import com.example.data.model.DateNote
import com.example.data.model.HolidayEntity
import com.example.data.model.RegularAlarm

@Database(
    entities = [RegularAlarm::class, DateAlarm::class, DateNote::class, HolidayEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun noteDao(): NoteDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `govt_holidays` (
                        `id` TEXT NOT NULL,
                        `countryCode` TEXT NOT NULL,
                        `countryName` TEXT NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `localName` TEXT NOT NULL,
                        `typesCsv` TEXT NOT NULL,
                        `isGlobal` INTEGER NOT NULL,
                        `reminderEnabled` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "datetime_calendar.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
