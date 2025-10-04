package com.example.ev_syatem.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EVChargingStation.db"
        private const val DATABASE_VERSION = 2 // Updated version

        // User Table
        const val TABLE_USER = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NIC = "nic"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_IS_ACTIVE = "is_active" // Changed from is_activate
        const val COLUMN_ROLE = "role"
        const val COLUMN_CREATED_AT_USER = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        // Reservation Table
        const val TABLE_RESERVATION = "reservations"
        const val COLUMN_RESERVATION_ID = "id"
        const val COLUMN_USER_NIC = "user_nic"
        const val COLUMN_STATION_NAME = "station_name"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CREATED_AT = "created_at"

        private const val CREATE_USER_TABLE = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NIC TEXT NOT NULL UNIQUE,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 1,
                $COLUMN_ROLE TEXT DEFAULT 'EvOwner',
                $COLUMN_CREATED_AT_USER TEXT NOT NULL,
                $COLUMN_UPDATED_AT TEXT
            )
        """

        private const val CREATE_RESERVATION_TABLE = """
            CREATE TABLE $TABLE_RESERVATION (
                $COLUMN_RESERVATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NIC TEXT NOT NULL,
                $COLUMN_STATION_NAME TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_USER_NIC) REFERENCES $TABLE_USER($COLUMN_NIC)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_RESERVATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")

        onCreate(db)
    }
}