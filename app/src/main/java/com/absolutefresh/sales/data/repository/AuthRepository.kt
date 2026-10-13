package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.StaffStatus
import com.absolutefresh.sales.data.model.User
import com.absolutefresh.sales.data.model.UserRole

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class InvalidCredentials(val message: String = "Incorrect phone/email or password.") : AuthResult()
    data class AccountInactive(val message: String = "Your account is inactive. Please contact your administrator.") : AuthResult()
    data class PendingApproval(val message: String = "Your account is pending administrator approval.") : AuthResult()
    data class OfflineNoCache(val message: String = "You need an internet connection the first time you sign in.") : AuthResult()
    data class NetworkError(val message: String = "Couldn't reach the server. Please check your connection.") : AuthResult()
}

interface AuthRepository {
    suspend fun signIn(identifier: String, password: String): AuthResult
    suspend fun getCachedUser(): User?
    suspend fun saveCachedUser(user: User)
    suspend fun clearSession()
}

class FakeAuthRepository(
    var simulatedResult: AuthResult? = null,
    private var cachedUser: User? = null
) : AuthRepository {

    override suspend fun signIn(identifier: String, password: String): AuthResult {
        simulatedResult?.let { return it }

        return when {
            identifier == "inactive" -> AuthResult.AccountInactive()
            identifier == "pending" -> AuthResult.PendingApproval()
            identifier == "offline" -> AuthResult.OfflineNoCache()
            identifier == "driver" && password == "pass123" -> AuthResult.Success(
                User(id = "drv-1", identifier = identifier, name = "Ramesh (Driver)", role = UserRole.DRIVER)
            )
            identifier.isNotEmpty() && password == "pass123" -> AuthResult.Success(
                User(id = "rep-1", identifier = identifier, name = "Sunil (Rep)", role = UserRole.REP)
            )
            else -> AuthResult.InvalidCredentials()
        }
    }

    override suspend fun getCachedUser(): User? = cachedUser

    override suspend fun saveCachedUser(user: User) {
        cachedUser = user
    }

    override suspend fun clearSession() {
        cachedUser = null
    }

    fun setCachedUser(user: User?) {
        cachedUser = user
    }
}
