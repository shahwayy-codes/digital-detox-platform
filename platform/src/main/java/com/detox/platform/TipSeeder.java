package com.detox.platform;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TipSeeder implements CommandLineRunner {

    private final TipRepository tipRepository;

    public TipSeeder(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }

    @Override
    public void run(String... args) {
        if (tipRepository.count() > 0) {
            return;
        }
        add("Charge your phone outside the bedroom",
                "Keep your charger in another room so the phone is not the first and last thing you touch each day.",
                "Tip");
        add("Switch your screen to grayscale",
                "Colours make apps addictive. A grayscale screen makes scrolling feel much less rewarding.",
                "Tip");
        add("Batch your social media",
                "Pick two fixed times a day to check social media instead of opening it whenever you feel bored.",
                "Tip");
        add("Take a walk without headphones",
                "Spend 20 minutes outside with no screen and no audio. Notice what is around you.",
                "Offline idea");
        add("Cook something from scratch",
                "Choose a recipe you have never tried and cook it without looking at your phone for anything else.",
                "Offline idea");
        add("Call a friend instead of texting",
                "A short voice call feels more connected than an hour of messages.",
                "Offline idea");
    }

    private void add(String title, String content, String type) {
        Tip tip = new Tip();
        tip.setTitle(title);
        tip.setContent(content);
        tip.setTipType(type);
        tipRepository.save(tip);
    }
}
