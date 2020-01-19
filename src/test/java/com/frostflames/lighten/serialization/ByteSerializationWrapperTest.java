package com.frostflames.lighten.serialization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteSerializationWrapperTest {

    @Test
    void testAll() {
        Object placeHolder = new Object();
        ByteSerializationWrapper<Object> byteSerializationWrapperUnderTest = new ByteSerializationWrapper<>(placeHolder);
        Assertions.assertEquals(byteSerializationWrapperUnderTest.getObject(), placeHolder);
    }
}
