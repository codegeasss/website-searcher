package com.company.seacher.utils;

import java.util.regex.Pattern;

public class Matcher {

    Pattern p;

    public Matcher(String term) {
        p = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
    }

    public boolean isMatch(String text) {
        return p.matcher(text).find();
    }
}
