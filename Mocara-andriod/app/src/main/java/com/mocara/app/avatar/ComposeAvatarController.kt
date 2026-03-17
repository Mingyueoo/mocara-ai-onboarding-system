package com.mocara.app.avatar

import com.mocara.app.domain.model.AvatarEmotion
import com.mocara.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ComposeAvatarController
 *
 * Current implementation using Compose UI
 * Displays avatar as chat bubbles with typing indicators
 *
 * Future: This will be replaced with SoulMachinesAvatarController
 * which integrates the Soul Machines SDK for realistic 3D avatars
 */
class ComposeAvatarController : AvatarUiController {

    private val _avatarState = MutableStateFlow(AvatarState())
    val avatarState: StateFlow<AvatarState> = _avatarState.asStateFlow()

    /**
     * Display a message from the avatar
     */
    override fun showMessage(message: ChatMessage) {
        _avatarState.value = _avatarState.value.copy(
            currentMessage = message,
            emotion = message.avatarEmotion,
            isEscalated = message.isEscalated,
            isSpeaking = false,
            isThinking = false
        )
    }

    /**
     * Start speaking animation
     */
    override fun startSpeaking() {
        _avatarState.value = _avatarState.value.copy(
            isSpeaking = true,
            isThinking = false
        )
    }

    /**
     * Stop speaking animation
     */
    override fun stopSpeaking() {
        _avatarState.value = _avatarState.value.copy(
            isSpeaking = false
        )
    }

    /**
     * Set avatar emotion
     */
    override fun setEmotion(emotion: AvatarEmotion) {
        _avatarState.value = _avatarState.value.copy(
            emotion = emotion
        )
    }

    /**
     * Show escalation state
     */
    override fun showEscalation() {
        _avatarState.value = _avatarState.value.copy(
            isEscalated = true,
            emotion = AvatarEmotion.SERIOUS,
            isSpeaking = false,
            isThinking = false
        )
    }

    /**
     * Show thinking indicator
     */
    override fun showThinking() {
        _avatarState.value = _avatarState.value.copy(
            isThinking = true,
            isSpeaking = false
        )
    }

    /**
     * Hide thinking indicator
     */
    override fun hideThinking() {
        _avatarState.value = _avatarState.value.copy(
            isThinking = false
        )
    }

    /**
     * Reset to neutral state
     */
    override fun reset() {
        _avatarState.value = AvatarState()
    }

    /**
     * Show greeting animation
     */
    override fun showGreeting() {
        _avatarState.value = _avatarState.value.copy(
            emotion = AvatarEmotion.HAPPY,
            isSpeaking = true
        )
    }

    /**
     * Show farewell animation
     */
    override fun showFarewell() {
        _avatarState.value = _avatarState.value.copy(
            emotion = AvatarEmotion.NEUTRAL,
            isSpeaking = true
        )
    }

    /**
     * Get current emotion icon
     * Used for visual representation in Compose UI
     */
    fun getEmotionIcon(): String {
        return when (_avatarState.value.emotion) {
            AvatarEmotion.NEUTRAL -> "😊"
            AvatarEmotion.HAPPY -> "😄"
            AvatarEmotion.CONCERNED -> "😟"
            AvatarEmotion.SERIOUS -> "😐"
            AvatarEmotion.EMPATHETIC -> "🤗"
        }
    }

    /**
     * Get current emotion description
     */
    fun getEmotionDescription(): String {
        return when (_avatarState.value.emotion) {
            AvatarEmotion.NEUTRAL -> "Neutral"
            AvatarEmotion.HAPPY -> "Happy"
            AvatarEmotion.CONCERNED -> "Concerned"
            AvatarEmotion.SERIOUS -> "Serious"
            AvatarEmotion.EMPATHETIC -> "Empathetic"
        }
    }
}

/**
 * Future Implementation Notes:
 *
 * When integrating Soul Machines SDK, create a new class:
 * class SoulMachinesAvatarController : AvatarUiController {
 *     private val soulMachinesSDK: SoulMachinesSDK
 *
 *     override fun showMessage(message: ChatMessage) {
 *         // Use Soul Machines SDK to animate avatar speaking
 *         soulMachinesSDK.speak(message.content)
 *         soulMachinesSDK.setEmotion(message.avatarEmotion)
 *     }
 *
 *     override fun setEmotion(emotion: AvatarEmotion) {
 *         // Map our emotion enum to Soul Machines emotion system
 *         val smEmotion = mapToSoulMachinesEmotion(emotion)
 *         soulMachinesSDK.setEmotion(smEmotion)
 *     }
 *
 *     // ... other implementations
 * }
 *
 * Benefits of this abstraction:
 * 1. Current app works with simple Compose UI
 * 2. Can swap to Soul Machines without changing ViewModels or Screens
 * 3. Can run A/B tests between implementations
 * 4. Easy to mock for testing
 */