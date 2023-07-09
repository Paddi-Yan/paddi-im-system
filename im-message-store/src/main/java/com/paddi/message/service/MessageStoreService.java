package com.paddi.message.service;

import com.paddi.common.model.message.DoStoreGroupMessageDTO;
import com.paddi.common.model.message.DoStoreP2PMessageDTO;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:38:09
 */
public interface MessageStoreService {

    void storeMessage(DoStoreP2PMessageDTO doStoreP2PMessageDTO);

    void storeGroupMessage(DoStoreGroupMessageDTO doStoreGroupMessageDTO);
}
