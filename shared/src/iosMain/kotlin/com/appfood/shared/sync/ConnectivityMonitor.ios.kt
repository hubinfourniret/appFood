package com.appfood.shared.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * iOS implementation of ConnectivityMonitor.
 * TODO: Wire with NWPathMonitor when DI is set up.
 */
actual class ConnectivityMonitor {
    private val _connected = MutableStateFlow(true)

    actual fun isConnected(): Boolean = _connected.value

    actual fun observeConnectivity(): Flow<Boolean> = _connected
}
