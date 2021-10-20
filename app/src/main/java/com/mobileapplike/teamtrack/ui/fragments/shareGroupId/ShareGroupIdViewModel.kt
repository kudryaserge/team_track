package com.mobileapplike.teamtrack.ui.fragments.shareGroupId

import androidx.lifecycle.ViewModel
import com.mobileapplike.teamtrack.ui.fragments.BaseViewModel
import com.mobileapplike.teamtrack.utils.FirebaseDB

class ShareGroupIdViewModel : BaseViewModel() {

    fun retrievePerson(){
        FirebaseDB.getPerson ({}, {
            person.postValue(it)
        })
    }

}