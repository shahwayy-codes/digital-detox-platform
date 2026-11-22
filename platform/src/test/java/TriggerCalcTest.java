package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TriggerCalcTest {

    @Test
    void cleanAcceptsKnownTriggersIgnoringCase() {
        assertEquals("Boredom", TriggerCalc.clean("boredom"));
        assertEquals("Work or study", TriggerCalc.clean("  WORK OR STUDY "));
    }

    @Test
    void cleanRejectsUnknownOrEmpty() {
        assertEquals(null, TriggerCalc.clean("hacking"));
        assertEquals(null, TriggerCalc.clean(""));
        assertEquals(null, TriggerCalc.clean(null));
    }

    @Test
    void breakdownCountsAndFindsTopTrigger() {
        TriggerCalc.Report r = TriggerCalc.breakdown(
                Arrays.asList("Boredom", "Boredom", "Stress", "Habit", null, "nonsense"));
        assertEquals(4, r.getTotal());
        assertTrue(r.isHasData());
        assertEquals(2, r.getRows().get(0).getCount());
        assertEquals(50, r.getRows().get(0).getPercent());
        assertEquals(100, r.getRows().get(0).getBarPercent());
        assertEquals(50, r.getRows().get(1).getBarPercent());
        assertTrue(r.getInsight().startsWith("Your top trigger is Boredom (2 of 4 check-ins, 50%)."));
    }

    @Test
    void breakdownWithNoDataAsksForTrigger() {
        TriggerCalc.Report r = TriggerCalc.breakdown(List.of());
        assertFalse(r.isHasData());
        assertEquals(6, r.getRows().size());
        assertTrue(r.getInsight().contains("Choose a trigger"));
    }
}
