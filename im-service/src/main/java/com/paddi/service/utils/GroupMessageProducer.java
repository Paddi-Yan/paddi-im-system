package com.paddi.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.pack.group.AddGroupMemberPackage;
import com.paddi.codec.pack.group.RemoveGroupMemberPackage;
import com.paddi.codec.pack.group.UpdateGroupMemberPackage;
import com.paddi.common.enums.ClientType;
import com.paddi.common.enums.command.Command;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.service.module.group.entity.vo.GroupMemberVO;
import com.paddi.service.module.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 20:26:34
 */
@Component
public class GroupMessageProducer {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private GroupMemberService groupMemberService;

    public void sendMessage(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject object = (JSONObject) JSONObject.toJSON(data);
        String groupId = object.getString("groupId");
        List<String> groupMemberId = groupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
        if(command.equals(GroupEventCommand.ADDED_MEMBER)) {
            //发送给管理员和被加入人本身
            List<GroupMemberVO> groupManager = groupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            AddGroupMemberPackage addGroupMemberPackage = object.toJavaObject(AddGroupMemberPackage.class);
            List<String> members = addGroupMemberPackage.getMembers();
            members.addAll(groupManager.stream().map(GroupMemberVO :: getMemberId).collect(Collectors.toList()));
            sendMessageByMemberType(userId, command, data, clientInfo, members);
        } else if(command.equals(GroupEventCommand.DELETED_MEMBER)) {
            RemoveGroupMemberPackage removeGroupMemberPackage = object.toJavaObject(RemoveGroupMemberPackage.class);
            groupMemberId.add(removeGroupMemberPackage.getMember());
            sendMessageByMemberType(userId, command, data, clientInfo, groupMemberId);
        } else if(command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            UpdateGroupMemberPackage updateGroupMemberPackage = object.toJavaObject(UpdateGroupMemberPackage.class);
            String memberId = updateGroupMemberPackage.getMemberId();
            sendMessageByMemberType(memberId, command, data, clientInfo, groupMemberId);
        }else {
            sendMessageByMemberType(userId, command, data, clientInfo, groupMemberId);
        }
    }

    private void sendMessageByMemberType(String userId, Command command, Object data, ClientInfo clientInfo,
                           List<String> groupMemberId) {
        for(String memberId : groupMemberId) {
            if(clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && userId.equals(memberId)) {
                messageProducer.sendToOtherUserTerminal(userId, command, data, clientInfo);
            } else {
                messageProducer.sendToAllUserTerminal(memberId, command, data, clientInfo.getAppId());
            }
        }
    }

}
