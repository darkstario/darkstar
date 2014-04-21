package io.darkstar.config;

import java.util.LinkedHashSet;
import java.util.Set;

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

    public static Set<String> setOf(String... strings) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for(String s : strings) {
            set.add(of(s));
        }
        return set;
    }
}
