package com.gorman.ourmemoryapp.data.auth.repository

import com.gorman.ourmemoryapp.data.auth.model.AuthUser
import com.gorman.ourmemoryapp.domain.models.AdminSession
import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.testutil.FakeAccountIndexRemoteDataSource
import com.gorman.ourmemoryapp.testutil.FakeAuthRemoteDataSource
import com.gorman.ourmemoryapp.testutil.FakeAuthRemoteDataSource.Companion.PASSWORD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthRepositoryImplTest {

    private val admin = AuthUser(uid = "admin-uid", email = "admin@memory.by", isAnonymous = false)
    private val editor = AuthUser(uid = "editor-uid", email = "editor@memory.by", isAnonymous = false)
    private val visitor = AuthUser(uid = "visitor-uid", email = "", isAnonymous = true)

    private val dataSource = FakeAuthRemoteDataSource(
        accounts = listOf(admin, editor).associateBy { it.email },
        adminUids = setOf(admin.uid, visitor.uid)
    )
    private val accountIndex = FakeAccountIndexRemoteDataSource()
    private val repository = AuthRepositoryImpl(dataSource, accountIndex)

    @Test
    fun signedOutUserIsNotAdmin() = runTest {
        assertEquals(AdminSession(), repository.observeSession().first())
    }

    @Test
    fun anonymousUserIsNotAdminEvenWithAdminRecord() = runTest {
        dataSource.user.value = visitor

        assertEquals(AdminSession(), repository.observeSession().first())
    }

    @Test
    fun userListedInAdminsIsAdmin() = runTest {
        dataSource.user.value = admin

        assertEquals(
            AdminSession(uid = admin.uid, email = admin.email, isAdmin = true),
            repository.observeSession().first()
        )
    }

    @Test
    fun removingAdminRecordRevokesRole() = runTest {
        dataSource.user.value = admin
        dataSource.admins.value = emptySet()

        assertEquals(false, repository.observeSession().first().isAdmin)
    }

    @Test
    fun signInReportsWrongCredentials() = runTest {
        assertEquals(SignInResult.WRONG_CREDENTIALS, repository.signIn(admin.email, "wrong"))
    }

    @Test
    fun signInAsAdminKeepsSession() = runTest {
        assertEquals(SignInResult.ADMIN, repository.signIn(admin.email, PASSWORD))
        assertEquals(admin, dataSource.user.value)
    }

    @Test
    fun signInWithoutAdminRecordSignsOut() = runTest {
        assertEquals(SignInResult.NOT_ADMIN, repository.signIn(editor.email, PASSWORD))
        assertNull(dataSource.user.value)
    }
}
