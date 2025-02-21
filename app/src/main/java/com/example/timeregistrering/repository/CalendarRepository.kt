package com.example.timeregistrering.repository

import android.content.Context
import android.util.Log
import com.example.timeregistrering.data.database.dao.MoedeDao
import com.example.timeregistrering.data.database.entity.MoedeEntity
import com.example.timeregistrering.util.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CalendarRepository"

@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moedeDao: MoedeDao,
    private val authRepository: AuthRepository
) {
    private var calendar: Calendar? = null
    private var googleSignInClient: GoogleSignInClient? = null

    init {
        setupGoogleSignIn()
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestScopes(com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_READONLY))
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            Log.d(TAG, "Google Sign-In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Google Sign-In", e)
        }
    }

    private fun initializeCalendarService(accessToken: String) {
        val credential = GoogleCredential().setAccessToken(accessToken)
        
        calendar = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("Timeregistrering")
        .build()
    }

    // Database funktioner
    fun getMeetings(): Flow<List<MoedeEntity>> = 
        moedeDao.getMeetings(
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(1)
        )

    // Google Calendar funktioner
    suspend fun syncMeetings(startDate: LocalDateTime, endDate: LocalDateTime) {
        fetchEvents(startDate, endDate)
            .collect { events ->
                events.forEach { event ->
                    val existingMoede = event.id?.let { moedeDao.getMeetingForGoogleEvent(it) }
                    
                    if (existingMoede == null) {
                        // Nyt møde fra Google Calendar
                        moedeDao.insertMeeting(
                            MoedeEntity(
                                titel = event.summary ?: "Unavngivet møde",
                                startTid = event.start.dateTime.toLocalDateTime(),
                                slutTid = event.end.dateTime.toLocalDateTime(),
                                beskrivelse = event.description,
                                googleEventId = event.id
                            )
                        )
                    } else {
                        // Opdater eksisterende møde hvis det er ændret
                        val opdateretMoede = existingMoede.copy(
                            titel = event.summary ?: "Unavngivet møde",
                            startTid = event.start.dateTime.toLocalDateTime(),
                            slutTid = event.end.dateTime.toLocalDateTime(),
                            beskrivelse = event.description
                        )
                        if (opdateretMoede != existingMoede) {
                            moedeDao.updateMeeting(opdateretMoede)
                        }
                    }
                }
            }
    }

    private suspend fun fetchEvents(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Event>> = flow {
        try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "No access token available")
                emit(emptyList())
                return@flow
            }

            initializeCalendarService(accessToken)
            
            val calendarService = calendar
            if (calendarService == null) {
                Log.e(TAG, "Calendar service not initialized")
                emit(emptyList())
                return@flow
            }

            val events = withContext(Dispatchers.IO) {
                calendarService.events().list("primary")
                    .setTimeMin(DateTime(startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setTimeMax(DateTime(endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
            }

            emit(events.items ?: emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching calendar events", e)
            emit(emptyList())
        }
    }

    private fun DateTime.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(this.value),
            ZoneId.systemDefault()
        )
    }
}
