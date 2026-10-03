package com.aadvik.chaivedapos.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE bills ADD COLUMN paidAmount REAL NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE bills ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT 'Unpaid'")
            database.execSQL("ALTER TABLE bills ADD COLUMN paymentMode TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE bills ADD COLUMN mobileNumber TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE bills ADD COLUMN tableNumber TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS printer_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    printerName TEXT NOT NULL,
                    paperSize TEXT NOT NULL,
                    connected INTEGER NOT NULL,
                    autoPrint INTEGER NOT NULL,
                    billLogo INTEGER NOT NULL,
                    billHeader TEXT NOT NULL,
                    billFooter TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE menu_products ADD COLUMN imageUri TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    /**
     * Version 7 -> 8:
     * Replace ONLY the menu table with the new CHAI VEDA master menu.
     * Bills, bill items, inventory, tables, restaurant settings and printer
     * settings are intentionally left untouched.
     */
    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            MenuSeed.replaceMenu(database)
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "chai_veda_database"
            )
                .addMigrations(
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(database: SupportSQLiteDatabase) {
                        super.onCreate(database)
                        // Fresh installation: seed the new menu once.
                        MenuSeed.replaceMenu(database)
                    }
                })
                .build()
                .also { INSTANCE = it }
        }
    }
}
