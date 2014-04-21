package io.darkstar.config;

public class IdentifierName {

    public static String of(String s) {

        if (s == null || "".equals(s)) {
            return s;
        }

        char[] chars = s.toCharArray();

        StringBuilder sb = new StringBuilder(chars.length);

        boolean first = true;
        boolean upperCaseNext = false;

        for (char c : chars) {
            if (c == '_' || Character.isWhitespace(c)) {
                if (!first) {
                    upperCaseNext = true;
                }
                continue;
            }

            if (first) {
                c = Character.toLowerCase(c);
                first = false;
            } else if (upperCaseNext) {
                c = Character.toUpperCase(c);
                upperCaseNext = false;
            }

            sb.append(c);
        }

        return sb.toString();

    }
}
