package com.appfood.shared.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * JVM stub — always connected (backend/server context).
 */
actual class ConnectivityMonitor {
    private val _connected = MutableStateFlow(true)

    actual fun isConnected(): Boolean = true

    actual fun observeConnectivity(): Flow<Boolean> = _connected
}
