package com.mocara.app.data.mock

import com.mocara.app.domain.model.*

/**
 * Mock Protocol Data
 * Provides fake protocol data for development
 * Future: Will be replaced by backend API
 */
object MockProtocolData {

    /**
     * Ozempic Onboarding Protocol
     */
    val ozempicProtocol = Protocol(
        id = "ozempic_onboarding_v1",
        drugId = "ozempic",
        drugName = "Ozempic (Semaglutide)",
        intent = ProtocolIntent.ONBOARDING,
        description = "GLP-1 receptor agonist for type 2 diabetes management",
        steps = listOf(
            ProtocolStep(
                stepNumber = 1,
                title = "Welcome to Ozempic",
                content = "Ozempic is a once-weekly injection for adults with type 2 diabetes. " +
                        "It helps improve blood sugar levels and may help with weight management. " +
                        "Let's walk through how to use it safely.",
                type = StepType.INFO
            ),
            ProtocolStep(
                stepNumber = 2,
                title = "Before You Start",
                content = "Have you discussed the following with your doctor?\n" +
                        "- Your medical history\n" +
                        "- Other medications you're taking\n" +
                        "- Allergies to medications\n" +
                        "- History of pancreatitis or thyroid problems",
                type = StepType.CONFIRMATION,
                requiresConfirmation = true,
                confirmationItems = listOf(
                    "I have discussed all required medical history items with my doctor."
                )
            ),

            ProtocolStep(
                stepNumber = 3,
                title = "Storage Instructions",
                content = "Store Ozempic properly:\n" +
                        "• Unopened pens: Refrigerate at 36°F to 46°F (2°C to 8°C)\n" +
                        "• After first use: Can be stored at room temperature (up to 86°F/30°C) or in refrigerator\n" +
                        "• Discard after 56 days of first use\n" +
                        "• Keep away from direct heat and light\n" +
                        "• Do NOT freeze",
                type = StepType.INFO
            ),
            ProtocolStep(
                stepNumber = 4,
                title = "Dosing Schedule",
                content = "Your starting dose will be determined by your healthcare provider. " +
                        "Typically starts at 0.25 mg once weekly for 4 weeks, then increases to 0.5 mg. " +
                        "Take on the same day each week, at any time with or without meals.",
                type = StepType.INFO
            ),
            ProtocolStep(
                stepNumber = 5,
                title = "Injection Sites",
                content = "Choose one injection site:\n" +
                        "• Abdomen (stomach area, except 2 inches around navel)\n" +
                        "• Thigh (front or sides)\n" +
                        "• Upper arm (back)\n\n" +
                        "Rotate injection sites each week to prevent irritation.",
                type = StepType.QUESTION,
                options = listOf("Abdomen", "Thigh", "Upper Arm")
            ),
            ProtocolStep(
                stepNumber = 6,
                title = "Important Side Effects",
                content = "Common side effects include nausea, vomiting, diarrhea, and constipation. " +
                        "These usually improve over time.\n\n" +
                        "⚠️ Contact your doctor immediately if you experience:\n" +
                        "• Severe abdominal pain\n" +
                        "• Vision changes\n" +
                        "• Signs of low blood sugar (sweating, shakiness, confusion)\n" +
                        "• Allergic reactions (rash, itching, difficulty breathing)",
                type = StepType.CONFIRMATION,
                requiresConfirmation = true,
                confirmationItems = listOf("I have read and understood the potential side effects.")
            ),
            ProtocolStep(
                stepNumber = 7,
                title = "Setup Complete",
                content = "You're ready to begin! Remember:\n" +
                        "• Take once weekly on the same day\n" +
                        "• Rotate injection sites\n" +
                        "• Monitor blood sugar as directed\n" +
                        "• Contact your healthcare provider with any concerns\n\n" +
                        "If you have any questions, I'm here to help!",
                type = StepType.CONFIRMATION,
                requiresConfirmation = true,
                confirmationItems = listOf("I know when to contact my healthcare provider.")

            )
        )
    )

    /**
     * Insulin Safety Protocol
     */
    val insulinSafetyProtocol = Protocol(
        id = "insulin_safety_v1",
        drugId = "insulin",
        drugName = "Insulin",
        intent = ProtocolIntent.SAFETY,
        description = "Critical safety information for insulin use",
        steps = listOf(
            ProtocolStep(
                stepNumber = 1,
                title = "Insulin Safety Check",
                content = "Before we continue, I need to ensure you're using insulin safely. " +
                        "This is critical for preventing serious complications.",
                type = StepType.INFO
            ),
            ProtocolStep(
                stepNumber = 2,
                title = "Have you been trained?",
                content = "Have you received proper training from a healthcare professional on how to:\n" +
                        "• Measure and inject insulin correctly\n" +
                        "• Check blood sugar levels\n" +
                        "• Recognize signs of low blood sugar\n" +
                        "• Store insulin properly",
                type = StepType.QUESTION,
                options = listOf("Yes, I've been trained", "No, I need training"),
                requiresConfirmation = true
            ),
            ProtocolStep(
                stepNumber = 3,
                title = "Blood Sugar Monitoring",
                content = "Do you have a blood glucose meter and know how to use it?",
                type = StepType.QUESTION,
                options = listOf("Yes", "No"),
                requiresConfirmation = true
            )
        )
    )

    /**
     * Generic Disease Information Protocol
     */
    val diseaseInfoProtocol = Protocol(
        id = "disease_info_v1",
        drugId = "general",
        drugName = "Disease Information",
        intent = ProtocolIntent.DISEASE,
        description = "General information about medical conditions",
        steps = listOf(
            ProtocolStep(
                stepNumber = 1,
                title = "Understanding Your Condition",
                content = "I can provide information about various medical conditions and treatments. " +
                        "What would you like to know about?",
                type = StepType.QUESTION,
                options = listOf(
                    "Medication information",
                    "Side effects",
                    "Lifestyle recommendations",
                    "When to contact doctor"
                )
            )
        )
    )

    /**
     * Get protocol by drug ID
     */
    fun getProtocolByDrugId(drugId: String): Protocol? {
        return when (drugId.lowercase()) {
            "ozempic", "semaglutide" -> ozempicProtocol
            "insulin" -> insulinSafetyProtocol
            else -> diseaseInfoProtocol
        }
    }

    /**
     * All available protocols
     */
    val allProtocols = listOf(
        ozempicProtocol,
        insulinSafetyProtocol,
        diseaseInfoProtocol
    )
}