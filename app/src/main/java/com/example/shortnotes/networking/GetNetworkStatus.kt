package com.example.shortnotes.networking

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun get_network_status(connectivityManager: ConnectivityManager): Boolean {
    val capabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return if (capabilities != null) {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    } else {
        false
    }
}
