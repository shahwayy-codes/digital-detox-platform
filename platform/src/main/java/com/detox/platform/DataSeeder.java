package com.detox.platform;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ChallengeRepository challengeRepository;

    public DataSeeder(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    public void run(String... args) {
        if (challengeRepository.count() > 0) {
            return;
        }
        challengeRepository.save(make("No Social Media Weekend", "Social media", 2,
                "Take a full weekend off every social media app and notice how much time you get back.",
                "Do not open any social media app today."));
        challengeRepository.save(make("Phone-Free Mornings", "Habits", 7,
                "Start your day without a screen. Use the first hour for yourself instead of scrolling.",
                "No phone for the first 60 minutes after waking up."));
        challengeRepository.save(make("Screen Time Under 3 Hours", "Screen time", 14,
                "Keep your total daily phone screen time under three hours for two weeks.",
                "Stay under 3 hours of screen time today."));
        challengeRepository.save(make("No Phone at Meals", "Mindfulness", 7,
                "Eat with full attention and spend meals with the people around you.",
                "Keep your phone away during every meal."));
        challengeRepository.save(make("Digital Sunset", "Sleep", 14,
                "Switch screens off before bed to sleep better and wake up fresher.",
                "No screens for the last hour before sleep."));
        challengeRepository.save(make("Notification Cleanse", "Focus", 3,
                "Turn off all non-essential notifications and take back control of your attention.",
                "Keep non-essential notifications switched off all day."));
        challengeRepository.save(make("Offline Hobby Week", "Offline", 7,
                "Replace scrolling time with a hobby that does not need a screen.",
                "Spend at least 30 minutes on an offline hobby."));
    }

    private Challenge make(String title, String category, int days, String description, String dailyGoal) {
        Challenge c = new Challenge();
        c.setTitle(title);
        c.setCategory(category);
        c.setDurationDays(days);
        c.setDescription(description);
        c.setDailyGoal(dailyGoal);
        return c;
    }
}
