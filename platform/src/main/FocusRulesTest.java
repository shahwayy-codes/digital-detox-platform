package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FocusRulesTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 13, 10, 0, 0);

    @Test
    void onlyAllowedDurationsAreValid() {
        assertTrue(FocusRules.validDuration(15));
        assertTrue(FocusRules.validDuration(30));
        assertTrue(FocusRules.validDuration(60));
        assertFalse(FocusRules.validDuration(45));
        assertFalse(FocusRules.validDuration(0));
        assertFalse(FocusRules.validDuration(-15));
    }

    @Test
    void remainingTimeCountsDown() {
        assertEquals(900, FocusRules.remainingSeconds(START, 15, START));
        assertEquals(600, FocusRules.remainingSeconds(START, 15, START.plusMinutes(5)));
        assertEquals(0, FocusRules.remainingSeconds(START, 15, START.plusMinutes(20)));
    }

    @Test
    void cannotCompleteTooEarly() {
        assertFalse(FocusRules.canComplete(START, 30, START.plusMinutes(10)));
        assertFalse(FocusRules.canComplete(START, 30, START.plusMinutes(29)));
    }

    @Test
    void canCompleteWhenTimeIsUpOrJustBefore() {
        assertTrue(FocusRules.canComplete(START, 30, START.plusMinutes(30)));
        assertTrue(FocusRules.canComplete(START, 30, START.plusMinutes(30).minusSeconds(2)));
        assertTrue(FocusRules.canComplete(START, 30, START.plusMinutes(45)));
    }

    @Test
    void pointsAreOnePerFiveMinutes() {
        assertEquals(3, FocusRules.points(15));
        assertEquals(6, FocusRules.points(30));
        assertEquals(12, FocusRules.points(60));
        assertEquals(0, FocusRules.points(0));
    }
}
