package com.timejar.app.api.supabase

import android.app.Application
import com.timejar.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Date,
    val sex: Int,
)

class Supabase : Application() {
    companion object {
        lateinit var client: SupabaseClient
            private set

        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun signUp(userEmail: String, userPassword: String, userFirstName: String, userLastName: String, userDateOfBirth: Date, sexString: String,
                   onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
            coroutineScope.launch {
                try {
                    client.gotrue.signUpWith(Email) {
                        email = userEmail
                        password = userPassword
                    }

                    val user = client.gotrue.retrieveUserForCurrentSession()
                    val sex = if (sexString === "male") 1 else 2 // male or female
                    val userInfo = User(user.aud,userFirstName, userLastName,userDateOfBirth, sex)
                    client.postgrest.from("users").insert(userInfo)

                    onSuccess() // Invoke the success callback
                } catch (e: Exception) {
                    onFailure(Exception("Signup failed"))
                    onFailure(e)
                }
            }
        }

        fun login(userEmail: String, userPassword: String,
                  onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
            coroutineScope.launch {
                try {
                    client.gotrue.loginWith(Email) {
                        email = userEmail
                        password = userPassword
                    }

                    onSuccess() // Invoke the success callback
                } catch (e: Exception) {
                    onFailure(Exception("Signup failed"))
                    onFailure(e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseKey = BuildConfig.SUPABASE_KEY

        client = createSupabaseClient(supabaseUrl, supabaseKey) {
            install(GoTrue)
        }
    }
}
