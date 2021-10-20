package com.mobileapplike.teamtrack.ui.fragments.mygroupforeman


import com.mobileapplike.teamtrack.ui.fragments.BaseViewModel
import com.mobileapplike.teamtrack.utils.FirebaseDB

class MyGroupForemansViewModel : BaseViewModel() {


    fun refresh() {
        FirebaseDB.getMastersGroup {
            my_group.postValue(it)
        }
    }



    fun removeUser(personToken: String) {
        FirebaseDB.setPersonMasterId(personToken, ""){
            refresh()
        }

    }

    fun getPersonToken(position: Int): String? {
        return my_group.value?.get(position)?.token
    }

    fun add(id: String) {
        FirebaseDB.getPersonForID(id, {
            it?.let {
                FirebaseDB.setPersonMasterId(it.token, FirebaseDB.person.masterId){
                    refresh()
                }
            }

        }, {

        })

    }

    fun checkMaster(oonSuccess: () -> Unit, askLeaveMasterDirections: (String) -> Unit) {
        if (FirebaseDB.person.masterId.isBlank()) {
            oonSuccess()
        } else {
            FirebaseDB.getPersonForID(
                    FirebaseDB.person.masterId,
                    {
                        val question =  "Do you want to leave foreman's group " + it?.nickName + " " +
                        FirebaseDB.person.masterId + "?"
                        askLeaveMasterDirections(question)
                    },
                    {
                        message.postValue(it)
                    },
            )
        }
    }

    fun setMyPersonMasterId(newMasterId: String, onSuccess: () -> Unit) {
        FirebaseDB.setPersonMasterId(FirebaseDB.token, "") {
            onSuccess()
        }
    }

}