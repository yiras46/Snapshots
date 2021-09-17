package com.curso.snapshots.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.curso.snapshots.MainActivity
import com.curso.snapshots.R
import com.curso.snapshots.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var mBinding:FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnLogout.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()

        mBinding.textViewName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinding.textViewEmail.text = FirebaseAuth.getInstance().currentUser?.email
    }

    private fun logout() {
        context?.let { itContext ->
            AuthUI.getInstance().signOut(itContext)
                .addOnCompleteListener {
                    Toast.makeText(itContext, getString(R.string.message_logout), Toast.LENGTH_SHORT).show()
                    (requireContext() as MainActivity).checkUserLoggin()
                }
        }
    }
}