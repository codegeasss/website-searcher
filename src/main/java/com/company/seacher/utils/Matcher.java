package com.company.seacher.utils;

import java.util.regex.Pattern;

/**
 * Helper class for string matching operation
 */
public class Matcher {

    Pattern p;

    public Matcher(String term) {
        p = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Does a simple case insensitive match of the term in the given text
     *
     * @param  text
     * @return true if a match is found
     */
    public boolean isMatch(String text) {
        return p.matcher(text).find();
    }
}
