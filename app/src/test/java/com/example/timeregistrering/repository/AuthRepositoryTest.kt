package com.example.timeregistrering.repository

import android.content.Context
import com.example.timeregistrering.auth.GoogleAuthManager
import com.example.timeregistrering.common.security.SecurityManager
import com.example.timeregistrering.database.dao.UserDao
import com.example.timeregistrering.model.User
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthRepositoryTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var googleAuthManager: GoogleAuthManager
    
    @MockK
    private lateinit var userDao: UserDao
    
    private lateinit var repository: AuthRepository
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AuthRepository(context, securityManager, googleAuthManager, userDao)
    }
    
    @Test
    fun `when signing in with Google then success`() = runTest {
        // Given
        val token = "test_token"
        val user = User(id = "1", name = "Test User", email = "test@example.com")
        coEvery { googleAuthManager.signInWithGoogle(any()) } returns user
        coEvery { userDao.insertUser(any()) } returns Unit
        
        // When
        val result = repository.signInWithGoogle(token)
        
        // Then
        assertEquals(user, result)
        coVerify { userDao.insertUser(user) }
    }
    
    @Test
    fun `when signing out then success`() = runTest {
        // Given
        coEvery { googleAuthManager.signOut() } returns Unit
        coEvery { userDao.deleteAllUsers() } returns Unit
        
        // When
        repository.signOut()
        
        // Then
        coVerify { 
            googleAuthManager.signOut()
            userDao.deleteAllUsers()
        }
    }
    
    @Test
    fun `test token management`() = runTest {
        // Given
        val testToken = "test_token"
        every { securityManager.getSecureString(any()) } returns testToken
        
        // When
        val token = repository.getAccessToken()
        
        // Then
        assertEquals(testToken, token)
        verify { securityManager.getSecureString("google_access_token") }
    }

    @Test
    fun `test clear tokens`() = runTest {
        // When
        repository.clearTokens()
        
        // Then
        verify { 
            securityManager.removeSecureValue("google_access_token")
            securityManager.removeSecureValue("google_refresh_token")
        }
    }
}
