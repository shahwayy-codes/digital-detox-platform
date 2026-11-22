package com.detox.platform;

import java.util.List;

/** Small helper to build safe CSV text. */
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Quotes a value when needed. A value that starts with = + - or @ gets a leading
     * apostrophe so Excel does not run it as a formula.
     */
    public static String cell(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static String row(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cell(values.get(i)));
        }
        return sb.toString();
    }
}
