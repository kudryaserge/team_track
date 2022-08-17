package com.mobileapplike.teamtrack.utils

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.geojson.Point
import com.mobileapplike.teamtrack.Person
import com.squareup.okhttp.Dispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.StringBuilder
import kotlin.math.max

object FirebaseDB {
    lateinit var person: Person
    public lateinit var token: String
    var lastLatitude : Float = 0.0f
    var lastLongitude  : Float= 0.0f


    private val personCollectionRef = Firebase.firestore.collection("person")

    fun getToken() = CoroutineScope(Dispatchers.IO).launch {
        token = FirebaseMessaging.getInstance().token.await().toString()
    }

    fun saveLocation(latitude: String, longitude: String, errorMessage: (String?) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        val currentlatitude = latitude.toFloat()
        val currentlongitude = longitude.toFloat()

        if (max(lastLatitude - currentlatitude, currentlatitude - lastLatitude) < 0.0005
                || max(lastLongitude - currentlongitude,  currentlongitude - lastLongitude) < 0.0005){
            return@launch
        }

        val newPersonMap = mutableMapOf<String, Any>()
        newPersonMap["latitude"] = latitude
        newPersonMap["longitude"] = longitude
        updatePerson(token, newPersonMap) {
            if (it.isNullOrBlank()){
                lastLatitude = latitude.toFloat()
                lastLongitude = longitude.toFloat()
            } else {
                errorMessage(it)
            }

        }
    }


    private fun updatePerson(
        token: String,
        newPersonMap: Map<String, Any>,
        errorMessage: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {

        val personQuery = personCollectionRef
            .whereEqualTo("token", token)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()

                    withContext(Dispatchers.Main) {
                        errorMessage(null)
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage(e.message)
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                errorMessage("No persons matched the query.")
            }
        }
    }

    fun getPerson(errorMessage: (String?) -> Unit, onSuccess: (Person?) -> Unit) = CoroutineScope(Dispatchers.IO).launch {

        if(!::token.isInitialized) {
            token = FirebaseMessaging.getInstance().token.await().toString()
        }

        try {
            val querySnapshot = personCollectionRef
                .whereEqualTo("token", token)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.toObject<Person>()?.let {
                    person = it
                    onSuccess(it)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage(e.message)
            }
        }
    }

    fun getPerson(id: String, onSuccess: (Person?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = personCollectionRef
                    .whereEqualTo("id", id)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.toObject<Person>()?.let {
                        withContext(Dispatchers.Main) {
                            onSuccess(it)
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onSuccess(null)
                    Log.d("Track", e.toString())
                }
            }
        }

    fun getPersonForID(id: String, onSuccess: (Person?) -> Unit, onError: (String) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = personCollectionRef
                    .whereEqualTo("id", id)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.toObject<Person>()?.let {
                        withContext(Dispatchers.Main) {
                            onSuccess(it)
                            return@withContext
                        }
                    }
                }

                if (querySnapshot.documents.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("Device with id " + id + " not found")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.toString())
                }
                Log.d("Track", e.toString())
            }
        }

    fun getPersonArray(masterId: String, onSuccess: (ArrayList<Person>?)-> Unit, onError: (String) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {

            val personArrayList: ArrayList<Person> = arrayListOf()

            try {
                val querySnapshot = personCollectionRef
                    .whereEqualTo("masterId", masterId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.toObject<Person>()?.let {
                        personArrayList.add(it)
                    }
                }

                withContext(Dispatchers.Main) {
                    onSuccess(personArrayList)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.toString())
                }
            }
        }

    fun createPersonID(unSuccess: (Unit) -> Unit) {
        val newPersonMap = mutableMapOf<String, Any>()
        newPersonMap["id"] = (100000..999999).random().toString()
        updatePerson(token, newPersonMap) {
            unSuccess(Unit)
        }
    }

    fun setMasterId(masterID: String, onSuccess: (Unit) -> Unit, onError: (String) -> Unit) {
        if (masterID.equals(person.id)){
            onError("You can't join your own group. Please input other group id")
            return
        }

        val newPersonMap = mutableMapOf<String, Any>()
        newPersonMap["masterId"] = masterID
        updatePerson(token, newPersonMap) {
            onSuccess(Unit)
        }
    }

    fun getPersonsGroup(onSuccess: (ArrayList<Person>?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {

            if (!FirebaseDB::person.isInitialized) {
                return@launch
            }
            if (person.id.isEmpty()) {
                return@launch
            }

            val personArrayList: ArrayList<Person> = arrayListOf()
            personArrayList.add(person)


            // CoroutineScope(Dispatchers.IO).launch {
            if (person.masterId.isEmpty()) {
                getPersonArray(person.id, {
                    it?.let {
                        personArrayList.addAll(it)
                        if (person.masterId.isEmpty()) {
                            onSuccess(personArrayList)
                        }
                    }
                }, {})
            } else {
                getPerson(person.masterId) {
                    it?.let { masterPerson ->
                        personArrayList.add(masterPerson)

                        getPersonArray(person.masterId, {
                            it?.let {
                                for (lperson in it) {
                                    if (!(lperson.id.equals(person.id))) {
                                        personArrayList.add(lperson)
                                    }
                                }
                                onSuccess(personArrayList)
                            }
                        }, {})
                    }
                }
            }
/*
            getPersonArray(person.id, {
                it?.let {
                    personArrayList.addAll(it)
                    if (person.masterId.isEmpty()) {
                        onSuccess(personArrayList)
                    } else {
                        getPerson(person.masterId) {
                            it?.let { masterPerson ->
                                personArrayList.add(masterPerson)

                                getPersonArray(person.masterId, {
                                    it?.let {
                                        for (lperson in it) {
                                            if (!(lperson.id.equals(person.id))) {
                                                personArrayList.add(lperson)
                                            }
                                        }
                                        onSuccess(personArrayList)
                                    }
                                }, {})
                            }
                        }
                    }
                }
            }, {

            })*/

        }

    fun getMastersGroup(onSuccess: (ArrayList<Person>?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {

            if (!FirebaseDB::person.isInitialized) {
                return@launch
            }
            if (person.id.isEmpty()) {
                return@launch
            }

            val personArrayList: ArrayList<Person> = arrayListOf()

            if (person.masterId.isEmpty()) {
                onSuccess(personArrayList)
            } else {
                personArrayList.add(person)
                getPerson(person.masterId) {
                    it?.let { masterPerson ->
                        personArrayList.add(masterPerson)

                        getPersonArray(person.masterId, {
                            it?.let {
                                for (lperson in it) {
                                    if (!(lperson.id.equals(person.id))) {
                                        personArrayList.add(lperson)
                                    }
                                }
                                onSuccess(personArrayList)
                            }
                        }, {})
                    }
                }
            }
        }

    fun getFollowersGroup(onSuccess: (ArrayList<Person>?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {

            if (!FirebaseDB::person.isInitialized) {
                return@launch
            }
            if (person.id.isEmpty()) {
                return@launch
            }

            val personArrayList: ArrayList<Person> = arrayListOf()

            getPersonArray(person.id, {
                it?.let {
                    for (lperson in it) {
                        if (!(lperson.id.equals(person.id))) {
                            personArrayList.add(lperson)
                        }
                    }
                   onSuccess(personArrayList)
                }
            }, {})

        }


    fun setPersonMasterId(personToken: String, masterID: String, onSuccess: (Unit?) -> Unit) {
        val newPersonMap = mutableMapOf<String, Any>()
        newPersonMap["masterId"] = masterID
        updatePerson(personToken, newPersonMap) {
            onSuccess(Unit)
        }
    }


}