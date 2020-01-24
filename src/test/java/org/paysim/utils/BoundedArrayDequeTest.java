package org.paysim.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundedArrayDequeTest {

    @Test
    void testDequeDropsOldValues() {
        BoundedArrayDeque<String> dq = new BoundedArrayDeque<>(3);
        dq.push("A");
        dq.push("B");
        dq.push("C");

        Assertions.assertEquals(3, dq.size());
        dq.push("D");
        Assertions.assertEquals(3, dq.size());
        Assertions.assertEquals("D", dq.peekFirst());
        Assertions.assertEquals("B", dq.peekLast());

        dq.pop();
        dq.pop();
        Assertions.assertEquals(1, dq.size());
        Assertions.assertEquals("B", dq.peekFirst());
        dq.push("A");
        Assertions.assertEquals("A", dq.peekFirst());
        Assertions.assertEquals("B", dq.peekLast());
    }
}
