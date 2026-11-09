package com.detox.platform;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChallengeForm {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or less")
    private String title;

    @NotBlank(message = "Category is required")
    @Size(max = 40, message = "Category must be 40 characters or less")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be 1000 characters or less")
    private String description;

    @NotBlank(message = "Daily goal is required")
    @Size(max = 300, message = "Daily goal must be 300 characters or less")
    private String dailyGoal;

    @NotNull(message = "Enter the number of days")
    @Min(value = 1, message = "Must be at least 1 day")
    @Max(value = 365, message = "Must be 365 days or less")
    private Integer durationDays;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(String dailyGoal) { this.dailyGoal = dailyGoal; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
}
