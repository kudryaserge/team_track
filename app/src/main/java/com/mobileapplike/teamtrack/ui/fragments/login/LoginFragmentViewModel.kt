package com.mobileapplike.teamtrack.ui.fragments.login


import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mobileapplike.teamtrack.Person
import com.mobileapplike.teamtrack.ui.fragments.BaseViewModel
import com.mobileapplike.teamtrack.utils.FirebaseDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragmentViewModel : BaseViewModel() {

    private val personCollectionRef = Firebase.firestore.collection("person")


    fun add(token: String, nickName: String, setupNotification: (Unit) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        var newToken = token
        if (newToken.isBlank()){
            newToken = FirebaseMessaging.getInstance().token.await().toString()
            val person = Person(newToken, nickName, "", "", "")
            savePerson(person)
        } else { //already exists
            val person = Person(newToken, nickName, "", "", "")
            val personMap = getNewPersonMap(nickName)
            updatePerson(person, personMap)
        }

        FirebaseDB.getPerson ({

        },{
            if (FirebaseDB.person.id.isBlank()){
                FirebaseDB.createPersonID(){
                    setupNotification(Unit)
                    }
            } else {
                setupNotification(Unit)
            }
         })

    }



    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                //message.postValue("Successfully saved data.")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                message.postValue(e.message)
                //Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun getNewPersonMap(nickName: String): Map<String, Any> {
       val map = mutableMapOf<String, Any>()
       map["nickName"] = nickName
       return map
    }

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {


        val personQuery = personCollectionRef

                .whereEqualTo("token", person.token)
                .get()
                .await()
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    personCollectionRef.document(document.id).set(
                            newPersonMap,
                            SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        message.postValue(e.message)
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                message.postValue("No persons matched the query.")
            }
        }
    }
}

