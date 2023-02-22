package com.haolin.haotool.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Ahaolin
 * @description: BigDecimal运算工具类
 * @create 2020-10-20 14:14
 */
public class DecimalUtils {

    private DecimalUtils() {
    }

    /**
     * 判断data中是否存在超出val的数据 (是否使用绝对值比较)
     *
     * @param isUseAbsoluteVal 是否使用绝对值比较
     * @param val              比较的数据
     * @param data
     */
    public static boolean isExistOver(boolean isUseAbsoluteVal, String val, List<String> data) {
        // FIXME 有异常情况无判断
        final BigDecimal valDecimal = DecimalUtils.toBigDecimal(val, true);
        if (isUseAbsoluteVal) {
            for (String v : data) {
                final BigDecimal decimal = DecimalUtils.abs(DecimalUtils.toBigDecimal(v, true), true);
                if (decimal.compareTo(valDecimal) > 0) return true;
            }
        }else {
            for (String v : data) {
                final BigDecimal decimal = DecimalUtils.toBigDecimal(v, true);
                if (decimal.compareTo(valDecimal) > 0) return true;
            }
        }
        return false;
    }

    /**
     * 判断data中的数据 获取最大值
     *  返回的最大值 将保留小数处理{@link BigDecimal#setScale(int, RoundingMode)}
     * @param data data为null 返回0
     * @param isUseAbsoluteVal 是否使用绝对值比较
     * @param newScale 保留的位数
     * @param roundingMode
     */
    public static String maxStr(List<String> data, boolean isUseAbsoluteVal, int newScale, RoundingMode roundingMode) {
        // FIXME 有异常情况无判断
        BigDecimal max = BigDecimal.ZERO;
        if (CollUtil.isEmpty(data)) return DecimalUtils.toPlainString(max.setScale(newScale, roundingMode), true);
        if (isUseAbsoluteVal) {
            for (String v : data) {
                final BigDecimal decimal = DecimalUtils.abs(DecimalUtils.toBigDecimal(v, true), true);
                if (decimal.compareTo(max) > 0) max = decimal;
            }
        } else {
            for (String v : data) {
                final BigDecimal decimal = DecimalUtils.toBigDecimal(v, true);
                if (decimal.compareTo(max) > 0) max = decimal;
            }
        }
        return DecimalUtils.toPlainString(max.setScale(newScale, roundingMode), true);
    }


    public static class BigDecimalUnit {
        /**
         * -1
         */
        public static final BigDecimal BIG_DECIMAL_M_1 = new BigDecimal("-1");

        /**
         * 十
         */
        public static final BigDecimal BIG_DECIMAL_TEN = new BigDecimal("10");

        /**
         * 百
         */
        public static final BigDecimal BIG_DECIMAL_ONE_HUNDRED = new BigDecimal("100");

        /**
         * 1万
         */
        public static final BigDecimal BIG_DECIMAL_TEN_THOUSAND = new BigDecimal("10000");

        /**
         * 1亿
         */
        public static final BigDecimal BIG_DECIMAL_HUNDRED_MILLION = new BigDecimal("100000000");
    }


    /**
     * 加法计算（result = x + y）
     *
     * @param x                  被加数（可为null）
     * @param y                  加数 （可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return 和 （可为null）
     */
    public static BigDecimal add(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? OptionalNullForZero(y) : y;
        }
        if (y == null) {
            return optionalNullToZero ? OptionalNullForZero(x) : x;
        }
        return x.add(y);
    }

    /**
     * 加法计算（result = x + y）
     *
     * @param x                  被加数（可为null）
     * @param y                  加数 （可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return 和 （可为null）
     */
    public static BigDecimal addRound_2HalF_UP(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? OptionalNullForZero(y) : y;
        }
        if (y == null) {
            return optionalNullToZero ? OptionalNullForZero(x) : x;
        }
        return x.add(y).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 加法计算（result = a + b + c + d）
     *
     * @param a                  被加数（可为null）
     * @param b                  加数（可为null）
     * @param c                  加数（可为null）
     * @param d                  加数（可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal （可为null）
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d, boolean optionalNullToZero) {
        BigDecimal ab = add(a, b, false);
        BigDecimal cd = add(c, d, false);
        BigDecimal result = add(ab, cd, false);
        return optionalNullToZero ? OptionalNullForZero(result) : result;
    }


    /**
     * 减法计算(result = x - y)
     *
     * @param x                  被减数（可为null）
     * @param y                  减数（可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal 差 （可为null）
     */
    public static BigDecimal subtract(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return x.subtract(y);
    }

    /**
     * 减法计算(result = x - y)
     *
     * @param x                  被减数（可为null）
     * @param y                  减数（可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal 差 （可为null）
     */
    public static BigDecimal subtractRound_2HalF_UP(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return x.subtract(y).setScale(2,RoundingMode.HALF_UP);
    }


    /**
     * 乘法计算(result = x × y)
     *
     * @param x                  乘数(可为null)
     * @param y                  乘数(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal 积
     */
    public static BigDecimal multiply(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return x.multiply(y);
    }

    public static BigDecimal multiply_2RoundUp(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null ) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        // 结果为0.000..时，不用科学计数法展示
        return stripTrailingZeros(x.multiply(y).setScale(2, BigDecimal.ROUND_HALF_UP), optionalNullToZero);
    }

    /**
     * 除法计算(result = x ÷ y)
     *
     * @param x                  被除数（可为null）
     * @param y                  除数（可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return 商 （可为null,四舍五入，默认保留20位小数）
     */
    public static BigDecimal divide(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null || y.compareTo(BigDecimal.ZERO) == 0) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        // 结果为0.000..时，不用科学计数法展示
        return stripTrailingZeros(x.divide(y, 20, BigDecimal.ROUND_HALF_UP), optionalNullToZero);
    }

    /**
     * 除法计算(result = x ÷ y)
     *
     * @param x                  被除数（可为null）
     * @param y                  除数（可为null）
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return 商 （可为null,四舍五入，默认保留20位小数）
     */
    public static BigDecimal divideRound_2HalF_UP(BigDecimal x, BigDecimal y, boolean optionalNullToZero) {
        if (x == null || y == null || y.compareTo(BigDecimal.ZERO) == 0) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        // 结果为0.000..时，不用科学计数法展示
        return stripTrailingZeros(x.divide(y, 2, BigDecimal.ROUND_HALF_UP), optionalNullToZero);
    }

    /**
     * 转为字符串(防止返回可续计数法表达式)
     *
     * @param x                  要转字符串的小数
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return String
     */
    public static String toPlainString(BigDecimal x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? "0" : null;
        }
        return x.stripTrailingZeros().toPlainString();
    }

    /**
     * 保留小数位数
     *
     * @param x                  目标小数
     * @param scale              要保留小数位数
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal 结果四舍五入
     */
    public static BigDecimal scale(BigDecimal x, int scale, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return x.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 整型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Integer x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return new BigDecimal(x.toString());
    }

    /**
     * 长整型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Long x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return new BigDecimal(x.toString());
    }

    /**
     * 双精度型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Double x, boolean optionalNullToZero) {
        return toBigDecimal(x, optionalNullToZero, true);
    }

    /**
     * 双精度型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @param isScienceNotation 是否解决科学计数法
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Double x, boolean optionalNullToZero, boolean isScienceNotation) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        final String numberStr = x.toString();
        boolean flag = numberStr.contains("E");
        if (flag && isScienceNotation) {
            return new BigDecimal(new BigDecimal(numberStr).toPlainString());
        }
        return new BigDecimal(numberStr);
    }

    /**
     * 单精度型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Float x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return new BigDecimal(x.toString());
    }

    /**
     * 字符串型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(String x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return new BigDecimal(x);
    }

    /**
     * 对象类型转为BigDecimal
     *
     * @param x(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal toBigDecimal(Object x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        if (x instanceof BigDecimal) return (BigDecimal) x;
        if (!NumberUtil.isNumber(String.valueOf(x))) return optionalNullToZero ? BigDecimal.ZERO : null;
        BigDecimal result = null;
        try {
            result = new BigDecimal(x.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 倍数计算，用于单位换算
     *
     * @param x                  目标数(可为null)
     * @param multiple           倍数 (可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     * @return BigDecimal (可为null)
     */
    public static BigDecimal multiple(BigDecimal x, Integer multiple, boolean optionalNullToZero) {
        if (x == null || multiple == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return DecimalUtils.multiply(x, toBigDecimal(multiple, false), optionalNullToZero);
    }

    /**
     * 去除小数点后的0（如: 输入1.000返回1）
     *
     * @param x                  目标数(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     */
    public static BigDecimal stripTrailingZeros(BigDecimal x, boolean optionalNullToZero) {
        if (x == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return x.stripTrailingZeros();
    }

    /**
     * 如果x为null  返回0
     */
    public static BigDecimal OptionalNullForZero(BigDecimal num) {
        return num == null ? BigDecimal.ZERO : num;
    }

    /**
     * 进行绝对值的判断
     * @param num 目标数(可为null)
     * @param optionalNullToZero 为true 如果为null 返回zero, 为false 如果为空返回null
     */
    public static BigDecimal abs(BigDecimal num, boolean optionalNullToZero) {
        if (num == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return num.abs();
    }

    /**
     * 判断是否为空或者zero
     * @param payReceiptAmount
     * @param optionalNullToZero 为true 如果payReceiptAmount为null 同样返回true 否则返回false
     * @return
     */
    public static boolean isNullOrZero(BigDecimal payReceiptAmount, boolean optionalNullToZero) {
        if (payReceiptAmount == null) {
            return optionalNullToZero;
        }
        return BigDecimal.ZERO.compareTo(payReceiptAmount) ==0 ;
    }

    /**
     * 判断是否为空或者zero
     * @param num
     * @param optionalNullToZero 为true 如果payReceiptAmount为null 返回false 否则返回true
     * @see DecimalUtils#isNullOrZero(BigDecimal, boolean)
     */
    public static boolean isNonNullOrZero(BigDecimal num, boolean optionalNullToZero){
        return !DecimalUtils.isNullOrZero(num,optionalNullToZero);
    }

    /**
     * 取反
     * @param num
     * @param optionalNullToZero 为true 如果num为null 返回0 否则返回null
     */
    public static BigDecimal negate(BigDecimal num, boolean optionalNullToZero) {
        if (num == null) {
            return optionalNullToZero ? BigDecimal.ZERO : null;
        }
        return num.multiply(BigDecimalUnit.BIG_DECIMAL_M_1);
    }


    public static boolean isPositiveNum(String num) {
        return isPositiveNum(DecimalUtils.toBigDecimal(num,true));
    }

    /**
     * @see DecimalUtils#isPositiveNum(BigDecimal, boolean, boolean)
     */
    public static boolean isPositiveNum(BigDecimal num) {
        return isPositiveNum(num,true,true);
    }

    /**
     * 判断num 属于正数还是负数
     * @param num
     * @param optionalNullToZero 如果为true, num is Null,相当于0, 否则走null 直接返回为false
     * @param zeroPositive 0是否为正数  为true 表示为正数
     * @return
     */
    public static boolean isPositiveNum(BigDecimal num, boolean optionalNullToZero, boolean zeroPositive) {
        if (num == null) {
            if (!optionalNullToZero) return false;
            num = BigDecimal.ZERO;
        }
        final int i = compareTo(num, BigDecimal.ZERO);
        if (i == 0) return zeroPositive;
        return i > 0;
    }

    /**
     * 如果两者其中一个为null  则默认为0
     * @see BigDecimal#compareTo(BigDecimal)
     * @param num1
     * @param num2
     * @return
     */
    public static int compareTo(BigDecimal num1, Double num2) {
        return toBigDecimal(num2, true).compareTo(OptionalNullForZero(num1));
    }

    /**
     * 如果两者其中一个为null  则默认为0
     * @see BigDecimal#compareTo(BigDecimal)
     * @param num1
     * @param num2
     * @return
     */
    public static int compareTo(BigDecimal num1, String num2) {
        return OptionalNullForZero(num1).compareTo(toBigDecimal(num2, true));
    }


    /**
     * 如果两者其中一个为null  则默认为0
     * @see BigDecimal#compareTo(BigDecimal)
     * @param num1
     * @param num2
     * @return
     */
    public static int compareTo(BigDecimal num1, BigDecimal num2) {
        return OptionalNullForZero(num1).compareTo(OptionalNullForZero(num2));
    }

    public static BigDecimal setScale(BigDecimal bigDecimal, int newScale,RoundingMode roundingMode){
        if (bigDecimal != null) {
            return bigDecimal.setScale(newScale,roundingMode);
        }
        return null;
    }

    public static BigDecimal setScale2RoundUp(BigDecimal bigDecimal) {
        return setScale(bigDecimal, 2, RoundingMode.HALF_UP);
    }

}