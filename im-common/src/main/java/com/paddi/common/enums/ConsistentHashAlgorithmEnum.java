package com.paddi.common.enums;

public enum ConsistentHashAlgorithmEnum {

    /**
     * TreeMap
     */
    TREE(1,"com.paddi.common.loadbalance.consistent.TreeMapConsistentHashAlgorithm"),

    /**
     * 自定义map
     */
    CUSTOMER(2,null),

    ;


    private int code;
    private String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static ConsistentHashAlgorithmEnum getAlgorithm(int ordinal) {
        for (int i = 0; i < ConsistentHashAlgorithmEnum.values().length; i++) {
            if (ConsistentHashAlgorithmEnum.values()[i].getCode() == ordinal) {
                return ConsistentHashAlgorithmEnum.values()[i];
            }
        }
        throw new RuntimeException("一致性哈希实现算法配置错误");
    }

    ConsistentHashAlgorithmEnum(int code, String clazz){
        this.code=code;
        this.clazz=clazz;
    }

    public String getClazz() {
        return clazz;
    }

    public int getCode() {
        return code;
    }
}
