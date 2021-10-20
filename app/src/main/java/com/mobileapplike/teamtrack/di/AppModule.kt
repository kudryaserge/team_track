package com.mobileapplike.teamtrack.di


import com.google.firebase.messaging.FirebaseMessaging
import com.mobileapplike.teamtrack.Person
import dagger.hilt.InstallIn
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ApplicationComponent


import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePerson(): Person {
        return runBlocking {
            Person(FirebaseMessaging.getInstance().token.await().toString(), "", "", "", "");
        }
    }


}