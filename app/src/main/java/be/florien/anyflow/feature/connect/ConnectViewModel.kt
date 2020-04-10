package be.florien.anyflow.feature.connect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import be.florien.anyflow.R
import be.florien.anyflow.data.server.AmpacheConnection
import be.florien.anyflow.data.server.exception.WrongIdentificationPairException
import be.florien.anyflow.extension.eLog
import be.florien.anyflow.feature.BaseViewModel
import be.florien.anyflow.feature.MutableValueLiveData
import be.florien.anyflow.feature.ValueLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main activity
 */
open class ConnectViewModel : BaseViewModel() {

    @Inject
    lateinit var ampacheConnection: AmpacheConnection

    /**
     * Fields
     */

    var server = MutableLiveData("")
    var username = MutableLiveData("")
    var password = MutableLiveData("")
    val isLoading: ValueLiveData<Boolean> = MutableValueLiveData(false)
    val isConnected: ValueLiveData<Boolean> = MutableValueLiveData(false)
    val errorMessage: LiveData<Int> = MutableLiveData(null)

    /**
     * Buttons calls
     */
    fun connect() {
        isLoading.mutable.value = true
        val serverUrl = server.value ?: return //todo warn user
        ampacheConnection.openConnection(serverUrl)
        if (username.value?.isBlank() == true) {
            viewModelScope.launch {

                try {
                    val it = ampacheConnection.ping()

                    isLoading.mutable.value = false
                    when (it.error.code) {
                        0 -> isConnected.mutable.value = true
                        else -> errorMessage.mutable.value = R.string.connect_error_extends
                    }
                } catch (it: Exception) {
                    isLoading.mutable.value = false
                    this@ConnectViewModel.eLog(it, "Error while extending session")
                }
            }
        } else {
            val password1 = password.value
            val user = username.value
            if (password1?.isNotBlank() == true && user?.isNotBlank() == true) {
                viewModelScope.launch {
                    try {
                        val it = ampacheConnection.authenticate(user, password1)
                        isLoading.mutable.value = false
                        when (it.error.code) {
                            0 -> isConnected.mutable.value = true
                            else -> errorMessage.mutable.value = R.string.connect_error_credentials
                        }
                    } catch (it: Exception) {
                        isLoading.mutable.value = false
                        when (it) {
                            is WrongIdentificationPairException -> {
                                this@ConnectViewModel.eLog(it, "Wrong username/password")
                                errorMessage.mutable.value = R.string.connect_error_credentials
                            }
                        }
                    }
                }
            }
        }
    }
}