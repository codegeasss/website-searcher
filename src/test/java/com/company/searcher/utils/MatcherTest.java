package com.company.searcher.utils;

import com.company.seacher.utils.Matcher;
import org.junit.Assert;
import org.junit.Test;

public class MatcherTest {

    @Test
    public void positiveMatchTest() {
        Matcher matcher = new Matcher("Lorem");
        Assert.assertEquals(matcher.isMatch("Lorem ipsum dolor sit amet"), true);
    }

    @Test
    public void positiveMatchCaseInsensitiveTest() {
        Matcher matcher = new Matcher("lorem");
        Assert.assertEquals(matcher.isMatch("Lorem ipsum dolor sit amet"), true);
    }

    @Test
    public void negativeMatchTest() {
        Matcher matcher = new Matcher("fox");
        Assert.assertEquals(matcher.isMatch("Lorem ipsum dolor sit amet"), false);
    }
}
