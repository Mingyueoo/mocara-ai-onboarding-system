package com.mocara.app.avatar

import com.mocara.app.domain.model.AvatarEmotion
import com.mocara.app.domain.model.ChatMessage

/**
 * AvatarUiController
 *
 * Abstract interface for controlling avatar presentation
 * Current: Compose-based UI
 * Future: Soul Machines SDK integration
 */
interface AvatarUiController {

    /**
     * Display a message from the avatar
     * @param message The message to display
     */
    fun showMessage(message: ChatMessage)

    /**
     * Show avatar speaking animation/state
     */
    fun startSpeaking()

    /**
     * Stop avatar speaking animation
     */
    fun stopSpeaking()

    /**
     * Set avatar emotion/expression
     * @param emotion The emotion to display
     */
    fun setEmotion(emotion: AvatarEmotion)

    /**
     * Show escalation state - avatar shows concern/seriousness
     */
    fun showEscalation()

    /**
     * Show thinking/processing indicator
     */
    fun showThinking()

    /**
     * Hide thinking indicator
     */
    fun hideThinking()

    /**
     * Reset avatar to neutral state
     */
    fun reset()

    /**
     * Show greeting animation
     */
    fun showGreeting()

    /**
     * Show farewell animation
     */
    fun showFarewell()
}

/**
 * Avatar State
 * Represents current state of the avatar
 */
data class AvatarState(
    val isSpeaking: Boolean = false,
    val isThinking: Boolean = false,
    val emotion: AvatarEmotion = AvatarEmotion.NEUTRAL,
    val currentMessage: ChatMessage? = null,
    val isEscalated: Boolean = false
)