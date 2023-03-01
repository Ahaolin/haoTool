package com.haolin.haotool.util;

import com.haolin.dubbo.common.function.Builder;
import com.haolin.haotool.bytecode.Form;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class BuilderTest {

    @Test
    public void testUser(){
        Form test = Builder.of(Form::new).with(Form::setData, "test").build();
        Assertions.assertEquals(test.getData(), "test");
    }
}

