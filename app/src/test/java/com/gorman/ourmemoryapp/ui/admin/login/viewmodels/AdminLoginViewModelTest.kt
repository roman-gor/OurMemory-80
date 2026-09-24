package com.gorman.ourmemoryapp.ui.admin.login.viewmodels

import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.testutil.FakeAuthRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginError
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminLoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun AdminLoginViewModel.fillForm() {
        onUiIntent(AdminLoginUiIntent.OnEmailChange("  admin@memory.by "))
        onUiIntent(AdminLoginUiIntent.OnPasswordChange("secret"))
    }

    @Test
    fun signInRequiresEmailAndPassword() {
        val viewModel = AdminLoginViewModel(FakeAuthRepository())

        assertFalse(viewModel.uiState.value.canSignIn)
        viewModel.fillForm()
        assertTrue(viewModel.uiState.value.canSignIn)
    }

    @Test
    fun adminSignInSendsTrimmedEmailAndCompletes() {
        val repository = FakeAuthRepository(result = SignInResult.ADMIN)
        val viewModel = AdminLoginViewModel(repository)
        viewModel.fillForm()

        viewModel.onUiIntent(AdminLoginUiIntent.OnSignInClick)

        assertEquals("admin@memory.by" to "secret", repository.signInAttempts.single())
        assertTrue(viewModel.uiState.value.isSignedIn)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun accountWithoutRightsShowsNoRightsError() {
        val viewModel = AdminLoginViewModel(FakeAuthRepository(result = SignInResult.NOT_ADMIN))
        viewModel.fillForm()

        viewModel.onUiIntent(AdminLoginUiIntent.OnSignInClick)

        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(AdminLoginError.NO_ADMIN_RIGHTS, viewModel.uiState.value.error)
    }

    @Test
    fun wrongPasswordShowsErrorUntilEdited() {
        val viewModel = AdminLoginViewModel(FakeAuthRepository(result = SignInResult.WRONG_CREDENTIALS))
        viewModel.fillForm()

        viewModel.onUiIntent(AdminLoginUiIntent.OnSignInClick)
        assertEquals(AdminLoginError.WRONG_CREDENTIALS, viewModel.uiState.value.error)

        viewModel.onUiIntent(AdminLoginUiIntent.OnPasswordChange("secret2"))
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun networkFailureShowsConnectionError() {
        val viewModel = AdminLoginViewModel(FakeAuthRepository(error = IllegalStateException("offline")))
        viewModel.fillForm()

        viewModel.onUiIntent(AdminLoginUiIntent.OnSignInClick)

        assertEquals(AdminLoginError.CONNECTION, viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.canSignIn)
    }
}
