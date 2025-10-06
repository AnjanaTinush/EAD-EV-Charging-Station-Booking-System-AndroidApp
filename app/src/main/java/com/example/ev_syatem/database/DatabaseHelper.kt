package com.example.ev_syatem.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EVChargingStation.db"
        private const val DATABASE_VERSION = 2

        // ====================== USER TABLE ======================
        const val TABLE_USER = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NIC = "nic"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_IS_ACTIVE = "is_active"
        const val COLUMN_ROLE = "role"
        const val COLUMN_CREATED_AT_USER = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        // ==================== RESERVATION TABLE ====================
        const val TABLE_RESERVATION = "reservations"
        const val COLUMN_RESERVATION_ID = "id"
        const val COLUMN_USER_NIC = "user_nic"
        const val COLUMN_STATION_NAME = "station_name"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CREATED_AT = "created_at"

        // ==================== CREATE TABLE QUERIES ====================
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
                $COLUMN_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_USER_NIC) REFERENCES $TABLE_USER($COLUMN_NIC)
            )
        """
    }

    // ==============================================================
    //                       DATABASE SETUP
    // ==============================================================

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_RESERVATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // ==============================================================
    //                    USER MANAGEMENT FUNCTIONS
    // ==============================================================

    // 🧩 Insert or replace logged-in user (used in login)
    fun insertUser(
        nic: String,
        fullName: String,
        email: String,
        phone: String,
        role: String,
        createdAt: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NIC, nic)
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_PASSWORD, "") // not storing actual password for security
            put(COLUMN_IS_ACTIVE, 1)
            put(COLUMN_ROLE, role)
            put(COLUMN_CREATED_AT_USER, createdAt)
        }
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // 🧩 Get user by NIC
    fun getUserByNic(nic: String): Map<String, String>? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USER,
            null,
            "$COLUMN_NIC = ?",
            arrayOf(nic),
            null,
            null,
            null
        )

        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                "nic" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                "full_name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                "role" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
            )
        }
        cursor.close()
        db.close()
        return user
    }

    // 🧩 Clear all users (used before inserting a new login)
    fun clearUsers() {
        val db = writableDatabase
        db.delete(TABLE_USER, null, null)
        db.close()
    }

    // 🧩 Get the most recently logged-in user (used in HomeActivity)
    fun getLatestUser(): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USER ORDER BY $COLUMN_ID DESC LIMIT 1", null)

        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                "nic" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                "full_name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                "role" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
            )
        }

        cursor.close()
        db.close()
        return user
    }
}
