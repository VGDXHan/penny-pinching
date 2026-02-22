package com.example.voicebill.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.voicebill.data.remote.DeepSeekApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

object ApiConstants {
    const val PREFS_NAME = "voicebill_secure_prefs"
    const val KEY_API_KEY = "deepseek_api_key"
}

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DeepSeekApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDeepSeekApi(retrofit: Retrofit): DeepSeekApi {
        return retrofit.create(DeepSeekApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext context: Context): SecurePrefs {
        return SecurePrefs(context)
    }
}

class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        ApiConstants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        val sanitizedKey = sanitizeApiKey(key)
        prefs.edit().putString(ApiConstants.KEY_API_KEY, sanitizedKey).apply()
    }

    fun getApiKey(): String? = prefs.getString(ApiConstants.KEY_API_KEY, null)
        ?.let(::sanitizeApiKey)
        ?.ifEmpty { null }

    fun hasApiKey(): Boolean = getApiKey() != null

    fun clearApiKey() {
        prefs.edit().remove(ApiConstants.KEY_API_KEY).apply()
    }

    // 统一移除所有空白字符，避免 Authorization 头中出现非法换行/空格
    private fun sanitizeApiKey(raw: String): String {
        return raw.filterNot { it.isWhitespace() }
    }
}
