package io.darkstar.config

import org.testng.annotations.Test
import static org.testng.Assert.*;

class IdentifierNameTest {

    @Test
    void testNames() {
        assertEquals IdentifierName.of('fooBarBaz'), 'fooBarBaz'
        assertEquals IdentifierName.of('FooBarBaz'), 'fooBarBaz'
        assertEquals IdentifierName.of('foo bar baz'), 'fooBarBaz'
        assertEquals IdentifierName.of('foo_bar_baz'), 'fooBarBaz'
        assertEquals IdentifierName.of('fooBar_baz'), 'fooBarBaz'
        assertEquals IdentifierName.of('.whatever.com'), '.whatever.com'
        assertEquals IdentifierName.of('*.whatever.com'), '*.whatever.com'
        assertEquals IdentifierName.of('foo.whatever.com'), 'foo.whatever.com'
    }
}
