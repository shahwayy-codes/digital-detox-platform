package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class QuestCalcTest {

    @Test
    void newUserMustJoinAChallengeFirst() {
        List<QuestCalc.Quest> q = QuestCalc.build(0, 0, false, false, 0, false);
        assertEquals(4, q.size());
        assertFalse(q.get(0).isDone());
        assertEquals("Join a challenge first", q.get(0).getDetail());
        assertEquals("/challenges", q.get(0).getLink());
        assertEquals(0, QuestCalc.doneCount(q));
    }

    @Test
    void pendingCheckInsAreCounted() {
        List<QuestCalc.Quest> q = QuestCalc.build(2, 1, true, true, 0, false);
        assertFalse(q.get(0).isDone());
        assertEquals("1 challenge is waiting", q.get(0).getDetail());
        List<QuestCalc.Quest> q2 = QuestCalc.build(3, 2, false, false, 0, false);
        assertEquals("2 challenges are waiting", q2.get(0).getDetail());
    }

    @Test
    void allDoneWhenEverythingIsCompleted() {
        List<QuestCalc.Quest> q = QuestCalc.build(2, 0, true, true, 30, true);
        assertEquals(4, QuestCalc.doneCount(q));
        assertEquals("15/15 min", q.get(2).getDetail());
    }

    @Test
    void goalQuestNeedsACheckInAndAMetGoal() {
        assertFalse(QuestCalc.build(1, 1, false, false, 0, false).get(1).isDone());
        assertEquals("Check in to find out", QuestCalc.build(1, 1, false, false, 0, false).get(1).getDetail());
        assertFalse(QuestCalc.build(1, 0, true, false, 0, false).get(1).isDone());
        assertTrue(QuestCalc.build(1, 0, true, true, 0, false).get(1).isDone());
    }

    @Test
    void focusProgressIsCappedAtTarget() {
        assertEquals("10/15 min", QuestCalc.build(1, 0, true, true, 10, false).get(2).getDetail());
        assertFalse(QuestCalc.build(1, 0, true, true, 10, false).get(2).isDone());
        assertTrue(QuestCalc.build(1, 0, true, true, 60, false).get(2).isDone());
        assertEquals("15/15 min", QuestCalc.build(1, 0, true, true, 60, false).get(2).getDetail());
    }
}
