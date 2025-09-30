package com.example.ev_syatem.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.ev_syatem.data.User
import com.example.ev_syatem.database.DatabaseHelper

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Register new user
    fun registerUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NIC, user.nic)
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_PASSWORD, user.password)
            put(DatabaseHelper.COLUMN_IS_ACTIVATE, if (user.isActivate) 1 else 0)
        }
        return db.insert(DatabaseHelper.TABLE_USER, null, values)
    }

    // Check if NIC already exists
    fun isNicExists(nic: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COLUMN_NIC),
            "${DatabaseHelper.COLUMN_NIC} = ?",
            arrayOf(nic),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Check if email already exists
    fun isEmailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COLUMN_EMAIL),
            "${DatabaseHelper.COLUMN_EMAIL} = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Login user
    fun loginUser(nic: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            null,
            "${DatabaseHelper.COLUMN_NIC} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?",
            arrayOf(nic, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // Get user by NIC
    fun getUserByNic(nic: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            null,
            "${DatabaseHelper.COLUMN_NIC} = ?",
            arrayOf(nic),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // Update user activation status
    fun updateUserActivation(nic: String, isActivate: Boolean): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_IS_ACTIVATE, if (isActivate) 1 else 0)
        }
        return db.update(
            DatabaseHelper.TABLE_USER,
            values,
            "${DatabaseHelper.COLUMN_NIC} = ?",
            arrayOf(nic)
        )
    }

    // Get all users
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_ID} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return users
    }

    // Helper function to convert cursor to User object
    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            nic = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NIC)),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)),
            isActivate = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVATE)) == 1
        )
    }

    // Update user
    fun updateUser(user: User): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_PASSWORD, user.password)
            put(DatabaseHelper.COLUMN_IS_ACTIVATE, if (user.isActivate) 1 else 0)
        }
        return db.update(
            DatabaseHelper.TABLE_USER,
            values,
            "${DatabaseHelper.COLUMN_NIC} = ?",
            arrayOf(user.nic)
        )
    }

    // Delete user
    fun deleteUser(nic: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_USER,
            "${DatabaseHelper.COLUMN_NIC} = ?",
            arrayOf(nic)
        )
    }
}