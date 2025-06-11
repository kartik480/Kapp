package com.kurakulas.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kurakulas.app.R
import com.kurakulas.app.databinding.FragmentProfileBinding
import com.kurakulas.app.ui.viewmodel.ProfileViewModel
import com.kurakulas.app.ui.viewmodel.ProfileUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ProfileUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.profileContent.visibility = View.GONE
                        }
                        is ProfileUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.profileContent.visibility = View.VISIBLE
                            updateUI(state.profile)
                        }
                        is ProfileUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.profileContent.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(profile: com.kurakulas.app.data.model.UserProfile) {
        with(binding) {
            tvName.text = "${profile.firstName} ${profile.lastName}"
            tvUsername.text = profile.username
            tvEmail.text = profile.emailId
            tvMobile.text = profile.mobile
            tvEmployeeNo.text = profile.employeeNo
            tvDepartment.text = profile.departmentId
            tvDesignation.text = profile.designationId
            tvJoiningDate.text = profile.joiningDate
            tvPresentAddress.text = profile.presentAddress
            tvPermanentAddress.text = profile.permanentAddress
            tvEmergencyContact.text = profile.emergencyNo
            tvEmergencyAddress.text = profile.emergencyAddress
            tvBankName.text = profile.bankName
            tvAccountNumber.text = profile.accountNumber
            tvIfscCode.text = profile.ifscCode
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 