package com.example.ev_syatem.utils

import com.example.ev_syatem.R

object AvatarUtils {
    /**
     * Returns a suitable avatar background drawable based on the user's name
     * This creates variety in avatar appearances
     */
    fun getAvatarBackground(userName: String): Int {
        val avatars = listOf(
            R.drawable.profile_avatar_gradient,  // Green
            R.drawable.avatar_background_blue,    // Blue
            R.drawable.avatar_background_purple, // Purple
            R.drawable.avatar_background_orange  // Orange
        )

        // Use the hash code of the name to consistently select the same avatar for the same user
        val index = Math.abs(userName.hashCode()) % avatars.size
        return avatars[index]
    }

    /**
     * Gets the initials from a full name (e.g., "John Doe" -> "JD")
     */
    fun getInitials(fullName: String): String {
        val names = fullName.trim().split(" ")
        return when {
            names.isEmpty() -> "?"
            names.size == 1 -> names[0].take(1).uppercase()
            else -> "${names.first().take(1)}${names.last().take(1)}".uppercase()
        }
    }
}