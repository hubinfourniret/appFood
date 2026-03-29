package com.appfood.shared.sync

import kotlinx.coroutines.flow.Flow

/**
 * Moniteur de connectivite reseau — expect/actual pour chaque plateforme.
 */
expect class ConnectivityMonitor {
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}
