package com.haolin.haotool.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DecimalUtilsTest {


    @Test
    void isExistOver() {
        List<String> data = Arrays.asList("100", "110", "-112");
        Assertions.assertTrue(DecimalUtils.isExistOver(true, "111", data));
        Assertions.assertFalse(DecimalUtils.isExistOver(false, "111", data));
    }

    @Test
    void maxStr() {
        List<String> data = Arrays.asList("100.484", "110.584", "-112.95");
        Assertions.assertEquals("0",DecimalUtils.maxStr(null, false, 2, RoundingMode.HALF_UP));
        Assertions.assertEquals("0",DecimalUtils.maxStr(new ArrayList<>(), false, 2, RoundingMode.HALF_UP));

        Assertions.assertEquals("110.58",
                DecimalUtils.maxStr(data, false, 2, RoundingMode.HALF_UP));
        Assertions.assertEquals("112.95",
                DecimalUtils.maxStr(data, true, 2, RoundingMode.HALF_UP));
    }

    @Test
    void add() {
        BigDecimal decimal1 = new BigDecimal("12.145");
        BigDecimal decimal2 = new BigDecimal("12.145");
        // 一个为null 返回另一个对象
        Assertions.assertEquals(BigDecimal.ZERO, DecimalUtils.add(null, null, true));
        Assertions.assertEquals(decimal1, DecimalUtils.add(null, decimal1, false));

        Assertions.assertEquals("24.290", DecimalUtils.add(decimal1, decimal2, false).toPlainString());
    }

    @Test
    void toPlainString() {
        BigDecimal decimal1 = new BigDecimal("12.145234550000");
        Assertions.assertEquals("12.14523455", DecimalUtils.toPlainString(decimal1, true));
    }




    @Test
    void isNullOrZero() {
        Assertions.assertTrue(DecimalUtils.isNullOrZero(null, true));
        Assertions.assertTrue(DecimalUtils.isNullOrZero(new BigDecimal("0"), true));
        Assertions.assertFalse(DecimalUtils.isNullOrZero(new BigDecimal("1"), true));
    }

    @Test
    void isPositiveNum() {
        Assertions.assertFalse(DecimalUtils.isPositiveNum(null, false,true));
        Assertions.assertFalse(DecimalUtils.isPositiveNum(null, false,false));

        Assertions.assertFalse(DecimalUtils.isPositiveNum(BigDecimal.ZERO, false,false));
        Assertions.assertTrue(DecimalUtils.isPositiveNum(BigDecimal.ZERO, false,true));

        Assertions.assertFalse(DecimalUtils.isPositiveNum(new BigDecimal("-1"), false,false));
        Assertions.assertTrue(DecimalUtils.isPositiveNum(new BigDecimal("1"), false,false));
    }
}