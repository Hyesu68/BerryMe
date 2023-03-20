package com.susuryo.berryme.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.susuryo.berryme.SplashActivity
import com.susuryo.berryme.databinding.ModalBottomSheetContentBinding

class ModalBottomSheet: BottomSheetDialogFragment() {
    private lateinit var binding: ModalBottomSheetContentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ModalBottomSheetContentBinding.inflate(inflater, container, false)
        binding.logout.setOnClickListener { setLogout() }
        return binding.root
    }

    private fun setLogout() {
        FirebaseAuth.getInstance().signOut()
        requireActivity().finish()
        startActivity(Intent(requireActivity(), SplashActivity::class.java))
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}