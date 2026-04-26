package com.mocara.app.di

import com.mocara.app.data.auth.TokenManager

object AppContainer {
    lateinit var tokenManager: TokenManager
        private set

    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }
}
