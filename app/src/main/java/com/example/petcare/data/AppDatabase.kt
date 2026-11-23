package com.example.petcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Pet::class, CareTask::class, CareHistory::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun careTaskDao(): CareTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN dueDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN dueTime TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN completedAt TEXT")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN reminderDateTime TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE care_tasks ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE care_history ADD COLUMN category TEXT NOT NULL DEFAULT 'General'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_care_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
