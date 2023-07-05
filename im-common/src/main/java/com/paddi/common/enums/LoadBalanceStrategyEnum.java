package com.paddi.common.enums;

public enum LoadBalanceStrategyEnum {

    /**
     * 随机
     */
    RANDOM(1,"com.paddi.common.loadbalance.random.RandomStrategy"),


    /**
     * 1.轮训
     */
    LOOP(2,"com.paddi.common.loadbalance.round.RoundRobinStrategy"),

    /**
     * HASH
     */
    HASH(3,"com.paddi.common.loadbalance.consistent.ConsistentHashStrategy"),
    ;


    private int code;
    private String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static LoadBalanceStrategyEnum getStrategy(int ordinal) {
        for (int i = 0; i < LoadBalanceStrategyEnum.values().length; i++) {
            if (LoadBalanceStrategyEnum.values()[i].getCode() == ordinal) {
                return LoadBalanceStrategyEnum.values()[i];
            }
        }
        throw new RuntimeException("负载均衡策略配置错误");
    }

    LoadBalanceStrategyEnum(int code, String clazz){
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
