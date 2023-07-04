package com.paddi.factory;

import com.paddi.common.enums.DeviceMultiLoginEnum;
import com.paddi.strategy.multi.MultiTerminalLoginProcessStrategy;
import com.paddi.strategy.multi.OneTerminalLoginProcessStrategy;
import com.paddi.strategy.multi.ThreeTerminalLoginProcessStrategy;
import com.paddi.strategy.multi.TwoTerminalLoginProcessStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 09:55:34
 */
public class MultiTerminalLoginProcessStrategyFactory {

    private static final Map<DeviceMultiLoginEnum, MultiTerminalLoginProcessStrategy> INSTANCE = new HashMap<>();
    static {
        INSTANCE.put(DeviceMultiLoginEnum.ONE, new OneTerminalLoginProcessStrategy());
        INSTANCE.put(DeviceMultiLoginEnum.TWO, new TwoTerminalLoginProcessStrategy());
        INSTANCE.put(DeviceMultiLoginEnum.THREE, new ThreeTerminalLoginProcessStrategy());
    }

    public static MultiTerminalLoginProcessStrategy getInstance(Integer type) {
        DeviceMultiLoginEnum key = DeviceMultiLoginEnum.getMember(type);
        return INSTANCE.get(key);
    }

}
