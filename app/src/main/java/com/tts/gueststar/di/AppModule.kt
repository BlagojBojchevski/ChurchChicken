package com.tts.gueststar.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideSharedPreferences():SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSpannableBuilder(): SpannableStringBuilder {
        return SpannableStringBuilder()
    }

    @Provides
    @Singleton
    fun getContext(): Context {
        return context
    }

//    @Provides
//    @Singleton
//    fun getOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
//        return OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .connectTimeout(30,TimeUnit.SECONDS).build()
//    }
//
//    @Provides
//    @Singleton
//    fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        return httpLoggingInterceptor
//    }
//
//
//    @Provides
//    @Singleton
//    fun getRetrofitNCR(): Retrofit {
//        return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL_VNCR)
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(getOkHttpClient(getHttpLoggingInterceptor()))
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideNCRApiService(): ApiService {
//        return getRetrofitNCR().create(ApiService::class.java)
//    }


}