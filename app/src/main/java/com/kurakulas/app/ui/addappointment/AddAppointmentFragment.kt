package com.kurakulas.app.ui.addappointment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import com.kurakulas.app.R
import com.kurakulas.app.data.model.PropertyDetailsRequest
import com.kurakulas.app.ui.viewmodel.AddAppointmentViewModel
import com.kurakulas.app.ui.viewmodel.AddAppointmentState

class AddAppointmentFragment : Fragment() {
    private val viewModel: AddAppointmentViewModel by viewModels()
    
    private lateinit var propertyTypeEditText: TextInputEditText
    private lateinit var areaEditText: TextInputEditText
    private lateinit var landsEditText: TextInputEditText
    private lateinit var sftEditText: TextInputEditText
    private lateinit var marketValueEditText: TextInputEditText

    // Appointment form fields
    private lateinit var mobileNumberEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var companyNameEditText: TextInputEditText
    private lateinit var alternativeMobileEditText: TextInputEditText
    private lateinit var stateEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var subLocationEditText: TextInputEditText
    private lateinit var pinCodeEditText: TextInputEditText
    private lateinit var sourceEditText: TextInputEditText
    private lateinit var qualificationEditText: TextInputEditText
    private lateinit var residentialAddressEditText: TextInputEditText
    private lateinit var customerTypeEditText: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_appointment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        initializeViews(view)
        
        // Observe ViewModel state
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        // Initialize all EditText fields
        propertyTypeEditText = view.findViewById(R.id.propertyTypeEditText)
        areaEditText = view.findViewById(R.id.areaEditText)
        landsEditText = view.findViewById(R.id.landsEditText)
        sftEditText = view.findViewById(R.id.sftEditText)
        marketValueEditText = view.findViewById(R.id.marketValueEditText)
        mobileNumberEditText = view.findViewById(R.id.mobileNumberEditText)
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        companyNameEditText = view.findViewById(R.id.companyNameEditText)
        alternativeMobileEditText = view.findViewById(R.id.alternativeMobileEditText)
        stateEditText = view.findViewById(R.id.stateEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        subLocationEditText = view.findViewById(R.id.subLocationEditText)
        pinCodeEditText = view.findViewById(R.id.pinCodeEditText)
        sourceEditText = view.findViewById(R.id.sourceEditText)
        qualificationEditText = view.findViewById(R.id.qualificationEditText)
        residentialAddressEditText = view.findViewById(R.id.residentialAddressEditText)
        customerTypeEditText = view.findViewById(R.id.customerTypeEditText)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Update UI based on state
                when {
                    state.isLoading -> {
                        // Show loading indicator
                    }
                    state.error != null -> {
                        // Show error message
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Handle success
                        Toast.makeText(context, "Appointment added successfully", Toast.LENGTH_SHORT).show()
                        // Navigate back or clear form
                    }
                }
            }
        }
    }
} 
