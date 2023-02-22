package com.haolin.haotool.bytecode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

class WrapperCheckTest {

    @Test
    public void testStrClean(){
        Form form = new Form();
        form.setData("");

        WrapperCheck wrapper = WrapperCheck.getWrapper(Form.class);
        wrapper.clearParamNonThrows(form, null);
//        try {
//            wrapper.clearParam(form);
//        } catch (NoSuchMethodException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
        Assertions.assertNull(form.getData());
    }

    @Test
    public void testListClean(){
        Form form = new Form();
        form.setTypes(new ArrayList<>());

        WrapperCheck wrapper = WrapperCheck.getWrapper(Form.class);
        try {
            wrapper.clearParam(form);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        Assertions.assertNull(form.getTypes());
    }

    @Test
    public void testStrNonClean(){
        Form form = new Form();
        form.setData("ces");

        WrapperCheck wrapper = WrapperCheck.getWrapper(Form.class);
        try {
            wrapper.clearParam(form);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(form.getData(),"ces");
    }
}