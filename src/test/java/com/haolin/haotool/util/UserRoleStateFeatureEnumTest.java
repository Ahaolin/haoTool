package com.haolin.haotool.util;

import cn.hutool.core.util.StrUtil;
import com.haolin.haotool.util.bitflag.StateFeature;
import org.junit.Assert;

public enum UserRoleStateFeatureEnumTest implements StateFeature {
    // 1 -> 00000001
    NORMAL("普通用户"),

    // 2 -> 00000010
    MANAGER("管理员"),

    // 4 -> 00000100
    SUPER_ADMIN("超级管理员"),
    ;

    private long mask;
    private String desc;

    UserRoleStateFeatureEnumTest(String desc) {
        this.desc = desc;
        this.mask = this.computeMask(this.ordinal());
    }

    // 新增角色 -》 位或操作
    // oldRole -> 00000001 -> 普通用户
    // addRole -> 00000010 -> 管理员
    // newRole -> 00000011 -> 普通用户、管理员
    public static Long addRole(long oldRole, long addRole) {
        return oldRole | addRole;
    }


    // 删除角色 -》 异或操作
    // allRole -> 00000011 -> 普通用户、管理员
    // qryRole -> 00000010 -> 删除管理员角色
    // resRole -> 00000001 -> 普通用户
    public static Long removeRole(long oldRole, long addRole) {
        return oldRole ^ addRole;
    }


    // 是否具有某种角色 -》 位与操作
    // allRole -> 00000011 -> 普通用户、管理员
    // qryRole -> 00000010 -> 查询是否具有管理员角色
    // resRole -> 00000001 -> 普通用户
    public static boolean hasRole(long role, long queryRole) {
        long resRole = role & queryRole;
        return queryRole == resRole;
    }


    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(
                    StrUtil.format("{}=== mask={}", 1, (1<<i))
            );
        }
        long state = UserRoleStateFeatureEnumTest.addRole(NORMAL.getMask(), MANAGER.getMask());
        System.out.println(state);
        System.out.println(Long.toBinaryString(state));

        Assert.assertTrue(StateFeature.isEnabled(state,NORMAL));
        Assert.assertEquals(StateFeature.config(state, NORMAL, false), MANAGER.getMask());
    }



    @Override
    public long getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
