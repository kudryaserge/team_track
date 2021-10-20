package com.mobileapplike.teamtrack.ui.fragments.map


import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mobileapplike.teamtrack.Person
import com.mobileapplike.teamtrack.ui.fragments.BaseViewModel
import com.mobileapplike.teamtrack.ui.fragments.jointhemaster.JoinTheMasterDirections
import com.mobileapplike.teamtrack.utils.FirebaseDB

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

//@ViewModelInject constructor(testString: String)
class MapViewModel
    : BaseViewModel() {


    private val personCollectionRef = Firebase.firestore.collection("person")

     fun retrievePersons(setupNotification: (Person?) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
         token = FirebaseMessaging.getInstance().token.await().toString();

        try {
            val querySnapshot = personCollectionRef
                .whereEqualTo("token", token)
                .get()
                .await()


            if (querySnapshot.documents.isEmpty()){
                withContext(Dispatchers.Main) {
                    setupNotification(null)
                }
            } else {
                for (document in querySnapshot.documents){
                    val lperson = document.toObject(Person::class.java)

                    person.postValue(lperson)
                    withContext(Dispatchers.Main) {
                        setupNotification(lperson)
                    }
                }
         }

        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                message.postValue(e.message)
            }
        }
    }

    fun subscribeToRealtimeUpdates() {

        personCollectionRef
            .whereEqualTo("token", token)
            .addSnapshotListener(){ querySnapshotToken, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    message.postValue(it.message)
                    return@addSnapshotListener
                }
                querySnapshotToken?.let {

                    for(document in it) {
                        val lperson = document.toObject<Person>()
                        person.postValue(lperson)
                        FirebaseDB.person = lperson
                    }

                }
            }



    }

    fun setMyPersonMasterId(newMasterId: String, onSuccess: () -> Unit) {
        FirebaseDB.setPersonMasterId(FirebaseDB.token, newMasterId) {
            onSuccess()
        }
    }

    fun checkFollowers(onSuccess: () -> Unit, askCloseGroup: (String, String) -> Unit) {

        FirebaseDB.getFollowersGroup {
            it?.let {
                if (it.size == 0 ){
                    onSuccess()
                } else {
                    var names: String = ""
                    for (person in it){
                        if (person.nickName.isBlank()) continue
                        if (it.first().equals(person)){
                            names = person.nickName
                        } else {
                            names = names + ", " + person.nickName
                        }
                        val question = "You already have some followers in your own group " + FirebaseDB.person.masterId+
                                ": " + names + "\n"+
                                "Do you want to close your own group and join to the master's group?"

                        val questionLeave = "You already have some followers in your own group " + FirebaseDB.person.masterId+
                                ": " + names + "\n"+
                                "Do you want to close your own group?"

                        askCloseGroup(question, questionLeave)

                    }

                }
            }

        }

    }

    fun checkMaster(oonSuccess: () -> Unit, askLeaveMasterDirections: (String) -> Unit) {
        if (FirebaseDB.person.masterId.isBlank()) {
            oonSuccess()
        } else {
            FirebaseDB.getPersonForID(
                FirebaseDB.person.masterId,
                {
                    val question =  "You have already joined to the master's group " + it?.nickName + " " +
                            FirebaseDB.person.masterId + ". Do you want to leave master's group and create your own group?"
                    askLeaveMasterDirections(question)
                },
                {
                    message.postValue(it)
                },
            )
        }
    }

    fun checkMasterForJoining(onSuccess: () -> Unit, askLeaveMasterDirections: (String, String) -> Unit) {
        if (FirebaseDB.person.masterId.isBlank()) {
            onSuccess()
        } else {
            FirebaseDB.getPersonForID(
                FirebaseDB.person.masterId,
                {
                    val question =  "You have already joined to the master's group " + it?.nickName + " " +
                            FirebaseDB.person.masterId + ". Do you want to leave this master's group and join to the new one?"
                    it?.let {
                        askLeaveMasterDirections(question, it.nickName)
                    }
                },
                {
                    message.postValue(it)
                },
            )
        }
    }

    fun closeMyGroup(onSuccess: () -> Unit) {
        FirebaseDB.getFollowersGroup {
            it?.let {
                for (person in it) {
                    FirebaseDB.setPersonMasterId(person.token, "") {

                    }
                }
            }
        }
        onSuccess()
    }


}