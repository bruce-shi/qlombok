package com.collectz.qlombok.javac.handler;

import com.collectz.qlombok.PresenceCheckTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PresenceFieldTest extends PresenceCheckTest {

    @Test
    @DisplayName("Generate Presence Field")
    public void presenceField() throws IOException {

        testClass("presence", "TestPresenceField");
    }

}
