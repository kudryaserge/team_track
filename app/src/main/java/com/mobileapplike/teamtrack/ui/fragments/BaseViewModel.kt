package com.mobileapplike.teamtrack.ui.fragments


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobileapplike.teamtrack.Person

open class BaseViewModel: ViewModel()  {
    val message = MutableLiveData<String>()
    val person =  MutableLiveData<Person>()
    lateinit var token: String
    val my_group =  MutableLiveData<ArrayList<Person>>()
}