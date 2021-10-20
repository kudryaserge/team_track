package com.mobileapplike.teamtrack.ui.fragments.mygroupforeman

import android.content.DialogInterface
import android.graphics.*
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.databinding.MyGroupForemansFragmentBinding
import com.mobileapplike.teamtrack.ui.MainActivity
import com.mobileapplike.teamtrack.ui.fragments.BaseFragment
import com.mobileapplike.teamtrack.ui.fragments.map.MapFragmentDirections
import com.mobileapplike.teamtrack.utils.TrackingUtility.drawableToBitmap
import com.mobileapplike.teamtrack.utils.TrackingUtility.isValidPhoneNumber
import kotlinx.android.synthetic.main.my_group_foremans_fragment.*

class MyGroupForemansFragment : BaseFragment() {

    companion object {
        fun newInstance() = MyGroupForemansFragment()
    }

    private lateinit var viewModel: MyGroupForemansViewModel
    var observableAdapter: ObservableAdapter = ObservableAdapter()
    private val p = Paint()
    lateinit var viewFragment: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_group_foremans_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewFragment = view

        viewModel = ViewModelProvider(this).get(MyGroupForemansViewModel::class.java)

        val binding = MyGroupForemansFragmentBinding.bind(view)

        viewModel.my_group.observe(viewLifecycleOwner,  {
            swtrlsObservablesUsers.isRefreshing = false
            observableAdapter.setData(it)
        })
        viewModel.refresh()

        binding.BackButton.setOnClickListener {
            findNavController().popBackStack()
        }


        swtrlsObservablesUsers.setOnRefreshListener {
            viewModel.refresh()
        }
        with(rvObservablesUsersList){
            layoutManager = LinearLayoutManager(context)
            adapter = observableAdapter
        }

        //initSwipe()

        fabLeave_group.setOnClickListener {
            viewModel.checkMaster({
               },
                    {

                        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                        builder.setMessage(it)
                                .setPositiveButton("Yes", dialogJoinLeaveClickListener)
                                .setNegativeButton("No", dialogJoinLeaveClickListener)
                                .show()
                    })



        }
    }

    var dialogJoinLeaveClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.setMyPersonMasterId(""){
                        findNavController().popBackStack()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }



            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    val personToken = viewModel.getPersonToken(position);
                    personToken?.let {
                        viewModel.removeUser(it)
                    }

                } else {

                    //current_phone_number = viewModel.getUser(position);
                    //displayAlert(current_phone_number)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX > 0) {
                        p.color = Color.parseColor("#388E3C")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = drawableToBitmap(resources.getDrawable(R.drawable.ic_edit_black_24dp))
                        val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    } else {
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = drawableToBitmap(resources.getDrawable(R.drawable.ic_delete_black_24dp))
                        val icon_dest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rvObservablesUsersList)
    }

    fun displayAlert(phoneNumber: String ){
        val alert = AlertDialog.Builder(requireContext())
        var editTextAge: EditText?=null
        var  okButton: Int = 0;


        // Builder
        with (alert) {
            setTitle("Edit phone number")
            if (phoneNumber.isEmpty()) setTitle("Add member")
            setCancelable(false)

            // Add any  input field here
            editTextAge= EditText(context)
            editTextAge!!.hint="123456"
            editTextAge!!.inputType = InputType.TYPE_CLASS_NUMBER

            editTextAge!!.setText("")

            setPositiveButton("Ok") {
                    dialog, whichButton ->
                //showMessage("display the game score or anything!")
                okButton = whichButton
                val new_phone_number= editTextAge!!.text.toString()

                if (new_phone_number.isEmpty()) {

                    view?.let { Snackbar.make(viewFragment, "Phone number must be not empty!", 1000).show() }
                    return@setPositiveButton
                }



                dialog.dismiss()

                if (phoneNumber.isEmpty()) {
                    viewModel.add(new_phone_number)
                } else {
                    //viewModel.edit(current_phone_number, new_phone_number)
                }
                observableAdapter.notifyDataSetChanged()

            }

            setNegativeButton("Cancel") {
                    dialog, whichButton ->
                //showMessage("Close the game or anything!")
                dialog.dismiss()
                observableAdapter.notifyDataSetChanged()
            }


        }

        // Dialog
        val dialog = alert.create()

        editTextAge!!.addTextChangedListener(object : PhoneNumberFormattingTextWatcher(){
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setEnabled(isValidPhoneNumber(s.toString()));
            }
        });

        //dialog.getButton(okButton).isEnabled = false
        dialog.setView(editTextAge)
        dialog.show()
    }

}