package org.microcol.model;

import org.junit.Test;

import mockit.Expectations;
import mockit.Tested;

public class ExampleTest {
    static class ClassA {
        String someMethod() { return ""; }

        void execute() {
            if ("expectedVar".equals(someMethod())) throw new MyException();
        }
    }

    static class MyException extends RuntimeException {}

    @Tested ClassA objA;

    @Test(expected = MyException.class)
    public void exampleTest() {
        final String expectedVar = "expectedVar";

        new Expectations(objA) {{ objA.someMethod(); result = expectedVar; }};

        objA.execute();
    }
}