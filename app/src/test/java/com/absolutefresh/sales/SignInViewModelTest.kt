package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.User
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.data.repository.AuthResult
import com.absolutefresh.sales.data.repository.FakeAuthRepository
import com.absolutefresh.sales.ui.auth.SignInScreenState
import com.absolutefresh.sales.ui.auth.SignInViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: SignInViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = SignInViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is an empty disabled form`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as SignInScreenState.Form
        assertEquals("", state.identifier)
        assertEquals("", state.password)
        assertFalse(state.isSubmitEnabled)
        assertFalse(state.isLoading)
        assertNull(state.inlineError)
    }

    @Test
    fun `submit disabled until both fields non-empty`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("rep@absolutefresh.com")
        var state = viewModel.uiState.value as SignInScreenState.Form
        assertFalse("Submit must be disabled when password is empty", state.isSubmitEnabled)

        viewModel.onIdentifierChanged("")
        viewModel.onPasswordChanged("pass123")
        state = viewModel.uiState.value as SignInScreenState.Form
        assertFalse("Submit must be disabled when identifier is empty", state.isSubmitEnabled)

        viewModel.onIdentifierChanged("rep@absolutefresh.com")
        viewModel.onPasswordChanged("pass123")
        state = viewModel.uiState.value as SignInScreenState.Form
        assertTrue("Submit must be enabled when both fields are filled", state.isSubmitEnabled)
    }

    @Test
    fun `no cached session on cold start stays on empty form`() = runTest {
        fakeRepository.setCachedUser(null)
        val freshVm = SignInViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = freshVm.uiState.value
        assertTrue(state is SignInScreenState.Form)
    }

    @Test
    fun `successful sign-in as rep routes with REP role`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("rep@absolutefresh.com")
        viewModel.onPasswordChanged("pass123")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.Authenticated
        assertEquals(UserRole.REP, state.role)
    }

    @Test
    fun `successful sign-in as driver routes with DRIVER role`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("driver")
        viewModel.onPasswordChanged("pass123")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.Authenticated
        assertEquals(UserRole.DRIVER, state.role)
    }

    @Test
    fun `cached session on cold start skips form and routes by role directly`() = runTest {
        val cached = User(id = "rep-cached", identifier = "rep1", name = "Cached Rep", role = UserRole.REP)
        fakeRepository.setCachedUser(cached)

        val freshVm = SignInViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = freshVm.uiState.value as SignInScreenState.Authenticated
        assertEquals(UserRole.REP, state.role)
        assertEquals("Cached Rep", state.userName)
    }

    @Test
    fun `invalid credentials clears password, keeps identifier, shows inline error`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("rep@absolutefresh.com")
        viewModel.onPasswordChanged("wrongpassword")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.Form
        assertEquals("rep@absolutefresh.com", state.identifier)
        assertEquals("", state.password)
        assertFalse(state.isSubmitEnabled)
        assertFalse(state.isLoading)
        assertEquals("Incorrect phone/email or password.", state.inlineError)
    }

    @Test
    fun `account inactive routes to blocking dead-end state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("inactive")
        viewModel.onPasswordChanged("pass123")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.AccountInactive
        assertTrue(state.message.contains("inactive", ignoreCase = true))
    }

    @Test
    fun `pending approval routes to blocking dead-end state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("pending")
        viewModel.onPasswordChanged("pass123")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.PendingApproval
        assertTrue(state.message.contains("pending", ignoreCase = true))
    }

    @Test
    fun `offline first login routes to offline dead-end with retry`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onIdentifierChanged("offline")
        viewModel.onPasswordChanged("pass123")
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SignInScreenState.OfflineDeadEnd
        assertTrue(state.message.contains("internet connection", ignoreCase = true))

        viewModel.retryFromOffline()
        val retriedState = viewModel.uiState.value
        assertTrue(retriedState is SignInScreenState.Form)
    }
}
