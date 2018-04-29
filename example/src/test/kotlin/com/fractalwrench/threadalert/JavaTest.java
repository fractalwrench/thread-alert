package com.fractalwrench.threadalert;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.junit.Test;

import static com.fractalwrench.threadalert.ThreadAlertKt.*;

public class JavaTest {

    @Test(expected = AssertionError.class)
    public void testDeadlock() {
        JavaDeadlock javaDeadlock = new JavaDeadlock();
        execute(new Function0<Unit>() { // Java 6 doesn't have lambdas :(
            @Override
            public Unit invoke() {
                javaDeadlock.hangForever();
                return null;
            }
        }).verify();
    }
}
