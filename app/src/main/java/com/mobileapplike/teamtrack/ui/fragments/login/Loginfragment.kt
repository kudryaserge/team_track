package com.mobileapplike.teamtrack.ui.fragments.login

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.databinding.LoginFragmentBinding
import com.mobileapplike.teamtrack.ui.fragments.BaseFragment
import com.mobileapplike.teamtrack.ui.fragments.map.MapFragmentDirections
import com.mobileapplike.teamtrack.utils.FirebaseDB


class LoginFragment : BaseFragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var loginFragmentViewModel: LoginFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginFragmentViewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)

        val binding = LoginFragmentBinding.bind(view)

        val args: LoginFragmentArgs by navArgs()

        binding.nickName.setText(args.nickName)
        binding.button.setOnClickListener {
            loginFragmentViewModel.add(
                args.token ?: "",
                binding.nickName.text.toString()
            ) {
                hideKeyboard()
                findNavController().popBackStack()
                if (args.nickName.isBlank() && !binding.nickName.text.isNullOrBlank()) {
                        findNavController().navigate(
                            MapFragmentDirections.actionMapFragmentToJoinTheMaster()
                        )
                }
            }
        }

        loginFragmentViewModel.message.observe(viewLifecycleOwner, {
            hideKeyboard()
            Snackbar.make(view, it, 1000).show()
        })


    }


}


fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}