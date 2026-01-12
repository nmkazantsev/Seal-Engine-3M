package com.nikitos.android;

import static org.junit.Assert.assertEquals;

import com.nikitos.TestClass;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void import_isCorrect() {
        assertEquals("Hello World!", TestClass.sayHello());
    }
}