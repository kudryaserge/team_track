package com.mobileapplike.teamtrack.ui.fragments.jointhemaster

import androidx.lifecycle.ViewModel
import com.mobileapplike.teamtrack.utils.FirebaseDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JoinTheMasterViewModel : ViewModel() {

    fun setMasterID(masterID: String, OnSuccess: (Unit) -> Unit, OnError: (String) -> Unit) {
        FirebaseDB.getPersonForID (masterID, { person->
            FirebaseDB.setMasterId(masterID, {
                OnSuccess(Unit)
            }, {errorString->
                OnError(errorString)
            })

        }, {
            OnError(it)
        })

    }

}