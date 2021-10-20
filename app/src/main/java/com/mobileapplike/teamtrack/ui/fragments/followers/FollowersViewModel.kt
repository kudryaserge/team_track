package com.mobileapplike.teamtrack.ui.fragments.followers

import androidx.lifecycle.ViewModel
import com.mobileapplike.teamtrack.ui.fragments.BaseViewModel
import com.mobileapplike.teamtrack.utils.FirebaseDB

class FollowersViewModel : BaseViewModel() {


    fun refresh() {
        FirebaseDB.getFollowersGroup {
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
                FirebaseDB.setPersonMasterId(it.token, FirebaseDB.person.id){
                    refresh()
                }
            }

        }, {

        })

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