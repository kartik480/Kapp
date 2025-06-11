package com.kurakulas.app.ui.addappointment

sealed class AddAppointmentState {
    object Initial : AddAppointmentState()
    object Loading : AddAppointmentState()
    data class Success(val message: String) : AddAppointmentState()
    data class Error(val message: String) : AddAppointmentState()
} 
