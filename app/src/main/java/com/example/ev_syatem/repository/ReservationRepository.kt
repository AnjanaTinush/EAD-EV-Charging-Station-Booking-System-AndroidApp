package com.example.ev_syatem.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.ev_syatem.data.Reservation
import com.example.ev_syatem.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class ReservationRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Create new reservation
    fun createReservation(reservation: Reservation): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NIC, reservation.userNic)
            put(DatabaseHelper.COLUMN_STATION_NAME, reservation.stationName)
            put(DatabaseHelper.COLUMN_DATE, reservation.date)
            put(DatabaseHelper.COLUMN_TIME, reservation.time)
            put(DatabaseHelper.COLUMN_STATUS, reservation.status)
            put(DatabaseHelper.COLUMN_CREATED_AT, reservation.createdAt)
        }
        return db.insert(DatabaseHelper.TABLE_RESERVATION, null, values)
    }

    // Get pending reservations count for a user
    fun getPendingReservationsCount(userNic: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_RESERVATION,
            arrayOf("COUNT(*)"),
            "${DatabaseHelper.COLUMN_USER_NIC} = ? AND ${DatabaseHelper.COLUMN_STATUS} = ?",
            arrayOf(userNic, "PENDING"),
            null, null, null
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // Get approved future reservations count for a user
    fun getApprovedFutureReservationsCount(userNic: String): Int {
        val db = dbHelper.readableDatabase
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val cursor = db.query(
            DatabaseHelper.TABLE_RESERVATION,
            arrayOf("COUNT(*)"),
            "${DatabaseHelper.COLUMN_USER_NIC} = ? AND ${DatabaseHelper.COLUMN_STATUS} = ? AND ${DatabaseHelper.COLUMN_DATE} >= ?",
            arrayOf(userNic, "APPROVED", currentDate),
            null, null, null
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // Get all reservations for a user
    fun getUserReservations(userNic: String): List<Reservation> {
        val reservations = mutableListOf<Reservation>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_RESERVATION,
            null,
            "${DatabaseHelper.COLUMN_USER_NIC} = ?",
            arrayOf(userNic),
            null, null,
            "${DatabaseHelper.COLUMN_CREATED_AT} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                reservations.add(cursorToReservation(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reservations
    }

    // Get reservation by ID
    fun getReservationById(id: Int): Reservation? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_RESERVATION,
            null,
            "${DatabaseHelper.COLUMN_RESERVATION_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val reservation = cursorToReservation(cursor)
            cursor.close()
            reservation
        } else {
            cursor.close()
            null
        }
    }

    // Update reservation status
    fun updateReservationStatus(id: Int, status: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, status)
        }
        return db.update(
            DatabaseHelper.TABLE_RESERVATION,
            values,
            "${DatabaseHelper.COLUMN_RESERVATION_ID} = ?",
            arrayOf(id.toString())
        )
    }

    // Delete reservation
    fun deleteReservation(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_RESERVATION,
            "${DatabaseHelper.COLUMN_RESERVATION_ID} = ?",
            arrayOf(id.toString())
        )
    }

    // Helper function to convert cursor to Reservation object
    private fun cursorToReservation(cursor: Cursor): Reservation {
        return Reservation(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_ID)),
            userNic = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NIC)),
            stationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATION_NAME)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
            time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
        )
    }
}
