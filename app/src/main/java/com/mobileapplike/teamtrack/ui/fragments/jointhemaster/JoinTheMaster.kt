package com.mobileapplike.teamtrack.ui.fragments.jointhemaster

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.databinding.JoinTheMasterFragmentBinding
import com.mobileapplike.teamtrack.ui.fragments.BaseFragment
import com.mobileapplike.teamtrack.ui.fragments.login.hideKeyboard
import com.mobileapplike.teamtrack.ui.fragments.map.MapFragmentDirections
import kotlinx.android.synthetic.main.share_group_id_fragment.*


class JoinTheMaster : BaseFragment()  {

    private lateinit var viewModel: JoinTheMasterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.join_the_master_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(JoinTheMasterViewModel::class.java)

        val binding = JoinTheMasterFragmentBinding.bind(view)
        binding.button.setOnClickListener {
            viewModel.setMasterID(binding.masterId.text.toString(), {
                hideKeyboard()
                findNavController().popBackStack()
            }, {
                hideKeyboard()
                Snackbar.make(view, it, Snackbar.LENGTH_LONG).show();
            })
        }

        binding.buttonCreate.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(MapFragmentDirections.actionMapFragmentToShareGroupIDFragment())
        }

        binding.BackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}