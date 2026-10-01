package com.mindflow.nova.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedUserEntity::class, CachedLevelsEntity::class, CachedMissionEntity::class,
        CachedProgressEntity::class, PendingAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun dao(): NovaDao

    companion object {
        // Excluida del respaldo en backup_rules.xml y data_extraction_rules.xml.
        const val NAME = "nova.db"

        fun create(context: Context): NovaDatabase =
            Room.databaseBuilder(context, NovaDatabase::class.java, NAME).build()
    }
}
