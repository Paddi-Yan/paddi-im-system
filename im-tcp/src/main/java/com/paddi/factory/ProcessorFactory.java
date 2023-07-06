package com.paddi.factory;

import com.paddi.processor.AbstractProcessor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 23:57:56
 */
public class ProcessorFactory {
    private static AbstractProcessor abstractProcessor;
    static {
        abstractProcessor = new AbstractProcessor() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static AbstractProcessor getMessageProcessor(Integer command) {
        //TODO 特定的command使用特定的Processor
        //if(command) { }
        return abstractProcessor;
    }
}
