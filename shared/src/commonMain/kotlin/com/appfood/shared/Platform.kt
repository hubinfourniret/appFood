package com.appfood.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
