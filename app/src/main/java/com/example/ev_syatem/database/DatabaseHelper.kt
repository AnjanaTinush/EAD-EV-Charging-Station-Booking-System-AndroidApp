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
        // ⬇️ Bump version because schema changes
        private const val DATABASE_VERSION = 3

        // ====================== USER TABLE ======================
        const val TABLE_USER = "users"
        const val COLUMN_ID = "id" // local row id (INTEGER PK)
        const val COLUMN_SERVER_ID = "server_id" // ⬅️ stores backend "id"
        const val COLUMN_USERNAME = "username"   // ⬅️ stores backend "username"
        const val COLUMN_NIC = "nic"
        const val COLUMN_FULL_NAME = "full_name" // keep for backward compatibility
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
                $COLUMN_SERVER_ID TEXT NOT NULL UNIQUE,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_NIC TEXT NOT NULL,
                $COLUMN_FULL_NAME TEXT, 
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT,
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

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_RESERVATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple reset migration (keeps things minimal)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // ==============================================================
    //                    USER MANAGEMENT FUNCTIONS
    // ==============================================================

    /**
     * Insert/replace the **logged-in** user from backend response.
     * @param serverId backend "id"
     * @param username backend "username"
     * @param email backend "email"
     * @param phone backend "phone"
     * @param nic backend "nic"
     * @param role backend "role"
     * @param isActive backend "isActive"
     * @param createdAt timestamp string
     */
    fun insertUser(
        serverId: String,
        username: String,
        email: String,
        phone: String,
        nic: String,
        role: String,
        isActive: Boolean,
        createdAt: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SERVER_ID, serverId)
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_NIC, nic)
            put(COLUMN_ROLE, role)
            put(COLUMN_IS_ACTIVE, if (isActive) 1 else 0)
            put(COLUMN_CREATED_AT_USER, createdAt)
            // Keep legacy fields consistent but optional
            put(COLUMN_FULL_NAME, username) // map username into full_name for now
            put(COLUMN_PASSWORD, "")        // never store plaintext password
        }
        // Replace by UNIQUE(server_id)
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // Get user by NIC (now also returns server fields)
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
                "server_id" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVER_ID)),
                "username" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                "nic" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                "full_name" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)) ?: ""),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                "role" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                "is_active" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)).toString()
            )
        }
        cursor.close()
        db.close()
        return user
    }

    // Get latest logged-in user (now includes server_id & username)
    fun getLatestUser(): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USER ORDER BY $COLUMN_ID DESC LIMIT 1", null)

        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                "server_id" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVER_ID)),
                "username" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                "nic" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                "full_name" to (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)) ?: ""),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                "role" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                "is_active" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)).toString()
            )
        }

        cursor.close()
        db.close()
        return user
    }

    fun clearUsers() {
        val db = writableDatabase
        db.delete(TABLE_USER, null, null)
        db.close()
    }

    fun isUserLoggedIn(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USER", null)
        var userExists = false
        if (cursor.moveToFirst()) {
            userExists = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return userExists
    }
}
