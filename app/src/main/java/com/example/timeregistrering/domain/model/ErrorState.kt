package com.example.timeregistrering.model

sealed class ErrorState {
    object None : ErrorState()
    data class NetworkError(val message: String) : ErrorState()
    data class CalendarError(val message: String) : ErrorState()
    data class DatabaseError(val message: String) : ErrorState()
    data class LocationError(val message: String) : ErrorState()
    data class UnknownError(val message: String) : ErrorState()

    companion object {
        fun fromException(e: Exception): ErrorState {
            return when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException -> NetworkError("Ingen internetforbindelse")
                is com.google.api.client.googleapis.json.GoogleJsonResponseException -> 
                    CalendarError("Google Calendar fejl: ${e.details.message}")
                is android.database.sqlite.SQLiteException -> 
                    DatabaseError("Database fejl: ${e.message}")
                is SecurityException -> 
                    LocationError("Lokation er ikke tilgængelig: ${e.message}")
                else -> UnknownError(e.message ?: "Ukendt fejl")
            }
        }
    }
}
