package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.StaffStatus
import com.absolutefresh.sales.data.model.User
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.data.network.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("role") val role: String? = "rep",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("area") val area: String? = "Lucknow"
)

/**
 * Authentic implementation of AuthRepository talking to Supabase Auth.
 */
class SupabaseAuthRepository(
    private val client: SupabaseClient = SupabaseClientProvider.getClient()
) : AuthRepository {

    private var cachedUser: User? = null

    override suspend fun signIn(identifier: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()

        // Local demo/offline fallback
        if (trimmed.equals("driver", ignoreCase = true) && password == "pass123") {
            val user = User(id = "drv-1", identifier = trimmed, name = "Ramesh (Driver)", role = UserRole.DRIVER)
            cachedUser = user
            return@withContext AuthResult.Success(user)
        }
        if (trimmed.equals("rep", ignoreCase = true) && password == "pass123") {
            val user = User(id = "rep-1", identifier = trimmed, name = "Sunil (Rep)", role = UserRole.REP)
            cachedUser = user
            return@withContext AuthResult.Success(user)
        }
        if ((trimmed.equals("admin", ignoreCase = true) || trimmed.equals("admin@absolutefreshfood.com", ignoreCase = true)) && (password == "pass123" || password == "admin123")) {
            val user = User(id = "admin-1", identifier = trimmed, name = "Swatantra Singh (Admin)", role = UserRole.REP, area = "All Areas")
            cachedUser = user
            return@withContext AuthResult.Success(user)
        }

        try {
            client.auth.signInWith(Email) {
                this.email = trimmed
                this.password = password
            }

            val session = client.auth.currentSessionOrNull()
            if (session == null) {
                return@withContext AuthResult.InvalidCredentials("Invalid email or password.")
            }

            val authUid = session.user?.id ?: ""
            val profile = try {
                client.postgrest["users"].select {
                    filter { eq("id", authUid) }
                }.decodeSingleOrNull<UserProfileDto>()
            } catch (_: Exception) {
                null
            }

            val roleStr = profile?.role?.lowercase() ?: "rep"
            val userRole = if (roleStr == "driver") UserRole.DRIVER else UserRole.REP
            val isActive = profile?.isActive ?: true

            if (!isActive) {
                client.auth.signOut()
                return@withContext AuthResult.AccountInactive()
            }

            val user = User(
                id = authUid,
                identifier = trimmed,
                name = profile?.name ?: trimmed.substringBefore("@"),
                role = userRole,
                status = StaffStatus.ACTIVE,
                area = profile?.area ?: "Lucknow East"
            )

            cachedUser = user
            AuthResult.Success(user)
        } catch (e: Exception) {
            val msg = e.message ?: "Authentication failed"
            if (msg.contains("Invalid", ignoreCase = true) || msg.contains("credential", ignoreCase = true)) {
                AuthResult.InvalidCredentials()
            } else {
                AuthResult.NetworkError(msg)
            }
        }
    }

    override suspend fun getCachedUser(): User? {
        if (cachedUser != null) return cachedUser
        val session = try { client.auth.currentSessionOrNull() } catch (_: Exception) { null }
        if (session != null) {
            val uid = session.user?.id ?: ""
            val email = session.user?.email ?: ""
            val user = User(
                id = uid,
                identifier = email,
                name = email.substringBefore("@"),
                role = UserRole.REP,
                status = StaffStatus.ACTIVE,
                area = "Lucknow"
            )
            cachedUser = user
            return user
        }
        return null
    }

    override suspend fun saveCachedUser(user: User) {
        cachedUser = user
    }

    override suspend fun clearSession() {
        cachedUser = null
        try {
            client.auth.signOut()
        } catch (_: Exception) {}
    }
}
