package com.mocara.backend.bootstrap;

import com.mocara.backend.common.enums.ProtocolIntent;
import com.mocara.backend.common.enums.StepType;
import com.mocara.backend.common.json.JsonListCodec;
import com.mocara.backend.protocol.entity.ProtocolEntity;
import com.mocara.backend.protocol.entity.ProtocolStepEntity;
import com.mocara.backend.protocol.repo.ProtocolRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the same protocol content as Android's MockProtocolData.kt.
 */
@Component
public class ProtocolSeeder implements CommandLineRunner {

    private final ProtocolRepository protocolRepository;
    private final JsonListCodec jsonListCodec;

    public ProtocolSeeder(ProtocolRepository protocolRepository, JsonListCodec jsonListCodec) {
        this.protocolRepository = protocolRepository;
        this.jsonListCodec = jsonListCodec;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedOzempic();
        seedInsulin();
        seedGeneralDisease();
    }

    private void seedOzempic() {
        ProtocolEntity p = new ProtocolEntity();
        p.setId("ozempic_onboarding_v1");
        p.setDrugId("ozempic");
        p.setDrugName("Ozempic (Semaglutide)");
        p.setIntent(ProtocolIntent.ONBOARDING);
        p.setDescription("GLP-1 receptor agonist for type 2 diabetes management");

        p.getSteps().clear();
        p.getSteps().add(step(p, 1, "Welcome to Ozempic",
                "Ozempic is a once-weekly injection for adults with type 2 diabetes. " +
                        "It helps improve blood sugar levels and may help with weight management. " +
                        "Let's walk through how to use it safely.",
                StepType.INFO, null, false, null));

        p.getSteps().add(step(p, 2, "Before You Start",
                "Have you discussed the following with your doctor?\n" +
                        "- Your medical history\n" +
                        "- Other medications you're taking\n" +
                        "- Allergies to medications\n" +
                        "- History of pancreatitis or thyroid problems",
                StepType.CONFIRMATION, null, true,
                List.of("I have discussed all required medical history items with my doctor.")));

        p.getSteps().add(step(p, 3, "Storage Instructions",
                "Store Ozempic properly:\n" +
                        "• Unopened pens: Refrigerate at 36°F to 46°F (2°C to 8°C)\n" +
                        "• After first use: Can be stored at room temperature (up to 86°F/30°C) or in refrigerator\n" +
                        "• Discard after 56 days of first use\n" +
                        "• Keep away from direct heat and light\n" +
                        "• Do NOT freeze",
                StepType.INFO, null, false, null));

        p.getSteps().add(step(p, 4, "Dosing Schedule",
                "Your starting dose will be determined by your healthcare provider. " +
                        "Typically starts at 0.25 mg once weekly for 4 weeks, then increases to 0.5 mg. " +
                        "Take on the same day each week, at any time with or without meals.",
                StepType.INFO, null, false, null));

        p.getSteps().add(step(p, 5, "Injection Sites",
                "Choose one injection site:\n" +
                        "• Abdomen (stomach area, except 2 inches around navel)\n" +
                        "• Thigh (front or sides)\n" +
                        "• Upper arm (back)\n\n" +
                        "Rotate injection sites each week to prevent irritation.",
                StepType.QUESTION, List.of("Abdomen", "Thigh", "Upper Arm"), false, null));

        p.getSteps().add(step(p, 6, "Important Side Effects",
                "Common side effects include nausea, vomiting, diarrhea, and constipation. " +
                        "These usually improve over time.\n\n" +
                        "⚠️ Contact your doctor immediately if you experience:\n" +
                        "• Severe abdominal pain\n" +
                        "• Vision changes\n" +
                        "• Signs of low blood sugar (sweating, shakiness, confusion)\n" +
                        "• Allergic reactions (rash, itching, difficulty breathing)",
                StepType.CONFIRMATION, null, true,
                List.of("I have read and understood the potential side effects.")));

        p.getSteps().add(step(p, 7, "Setup Complete",
                "You're ready to begin! Remember:\n" +
                        "• Take once weekly on the same day\n" +
                        "• Rotate injection sites\n" +
                        "• Monitor blood sugar as directed\n" +
                        "• Contact your healthcare provider with any concerns\n\n" +
                        "If you have any questions, I'm here to help!",
                StepType.CONFIRMATION, null, true,
                List.of("I know when to contact my healthcare provider.")));

        protocolRepository.save(p);
    }

    private void seedInsulin() {
        ProtocolEntity p = new ProtocolEntity();
        p.setId("insulin_safety_v1");
        p.setDrugId("insulin");
        p.setDrugName("Insulin");
        p.setIntent(ProtocolIntent.SAFETY);
        p.setDescription("Critical safety information for insulin use");

        p.getSteps().clear();
        p.getSteps().add(step(p, 1, "Insulin Safety Check",
                "Before we continue, I need to ensure you're using insulin safely. " +
                        "This is critical for preventing serious complications.",
                StepType.INFO, null, false, null));

        p.getSteps().add(step(p, 2, "Have you been trained?",
                "Have you received proper training from a healthcare professional on how to:\n" +
                        "• Measure and inject insulin correctly\n" +
                        "• Check blood sugar levels\n" +
                        "• Recognize signs of low blood sugar\n" +
                        "• Store insulin properly",
                StepType.QUESTION, List.of("Yes, I've been trained", "No, I need training"), true, null));

        p.getSteps().add(step(p, 3, "Blood Sugar Monitoring",
                "Do you have a blood glucose meter and know how to use it?",
                StepType.QUESTION, List.of("Yes", "No"), true, null));

        protocolRepository.save(p);
    }

    private void seedGeneralDisease() {
        ProtocolEntity p = new ProtocolEntity();
        p.setId("disease_info_v1");
        p.setDrugId("general");
        p.setDrugName("Disease Information");
        p.setIntent(ProtocolIntent.DISEASE);
        p.setDescription("General information about medical conditions");

        p.getSteps().clear();
        p.getSteps().add(step(p, 1, "Understanding Your Condition",
                "I can provide information about various medical conditions and treatments. " +
                        "What would you like to know about?",
                StepType.QUESTION,
                List.of("Medication information", "Side effects", "Lifestyle recommendations", "When to contact doctor"),
                false,
                null));

        protocolRepository.save(p);
    }

    private ProtocolStepEntity step(
            ProtocolEntity protocol,
            int stepNumber,
            String title,
            String content,
            StepType type,
            List<String> options,
            boolean requiresConfirmation,
            List<String> confirmationItems
    ) {
        ProtocolStepEntity s = new ProtocolStepEntity();
        s.setProtocol(protocol);
        s.setStepNumber(stepNumber);
        s.setTitle(title);
        s.setContent(content);
        s.setType(type);
        s.setRequiresConfirmation(requiresConfirmation);
        s.setOptionsJson(jsonListCodec.toJson(options));
        s.setConfirmationItemsJson(jsonListCodec.toJson(confirmationItems));
        return s;
    }
}

