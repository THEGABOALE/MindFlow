package com.mindflow.nova.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        CachedUserEntity::class, CachedLevelsEntity::class, CachedMissionEntity::class,
        CachedProgressEntity::class, PendingAttemptEntity::class, RejectedNoticeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun dao(): NovaDao

    companion object {
        // Excluida del respaldo en backup_rules.xml y data_extraction_rules.xml.
        const val NAME = "nova.db"

        /** La versión 2 suma los avisos de rechazo; los pendientes de la versión 1 se conservan. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rejected_notice` (`clientAttemptId` TEXT NOT NULL, " +
                        "`userId` INTEGER NOT NULL, `message` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`clientAttemptId`))"
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_rejected_notice_userId` ON `rejected_notice` (`userId`)"
                )
            }
        }

        /** La versión 3 guarda la pausa del reloj y el potenciador de cada pendiente. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `pending_attempt` ADD COLUMN `pausedSeconds` INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE `pending_attempt` ADD COLUMN `usedExtraTime` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun create(context: Context): NovaDatabase =
            Room.databaseBuilder(context, NovaDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
