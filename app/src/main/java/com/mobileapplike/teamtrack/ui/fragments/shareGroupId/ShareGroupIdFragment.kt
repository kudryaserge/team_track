package com.mobileapplike.teamtrack.ui.fragments.shareGroupId

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobileapplike.teamtrack.BuildConfig
import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.ui.fragments.BaseFragment
import kotlinx.android.synthetic.main.share_group_id_fragment.*

class ShareGroupIdFragment : BaseFragment() {

    companion object {
        fun newInstance() = ShareGroupIdFragment()
    }

    private lateinit var viewModel: ShareGroupIdViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.share_group_id_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ShareGroupIdViewModel::class.java)

        viewModel.retrievePerson()

        viewModel.person.observe(viewLifecycleOwner, {
            groupId.text = it.id
        })

        shareButton.setOnClickListener {


            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Application for sharing location")
            var shareMessage = "\nLet me recommend you this application\n"
            val myGroupIdText = "\nJoin my group: " + groupId.text

            shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                ${myGroupIdText}
                 """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share app via"))




        }

        BackButton.setOnClickListener {
            findNavController().popBackStack()
        }



    }

}