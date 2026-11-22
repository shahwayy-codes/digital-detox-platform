package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvUtilTest {

    @Test
    void plainTextIsLeftAlone() {
        assertEquals("Phone-Free Mornings", CsvUtil.cell("Phone-Free Mornings"));
    }

    @Test
    void nullBecomesEmpty() {
        assertEquals("", CsvUtil.cell(null));
    }

    @Test
    void commasAndQuotesAreEscaped() {
        assertEquals("\"Good, but \"\"tired\"\"\"", CsvUtil.cell("Good, but \"tired\""));
    }

    @Test
    void newlinesAreQuoted() {
        assertEquals("\"line1\nline2\"", CsvUtil.cell("line1\nline2"));
    }

    @Test
    void formulaLikeTextIsNeutralised() {
        assertEquals("'=SUM(A1)", CsvUtil.cell("=SUM(A1)"));
        assertEquals("'@cmd", CsvUtil.cell("@cmd"));
    }

    @Test
    void rowJoinsWithCommas() {
        assertEquals("2026-11-13,Challenge,Yes,95,\"a, b\"",
                CsvUtil.row(List.of("2026-11-13", "Challenge", "Yes", "95", "a, b")));
    }
}
