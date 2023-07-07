package com.paddi.service.module.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 13:15:10
 */
@Data
@TableName(value = "app_sign_record")
public class AppSignRecord {

    @TableId
    private Integer appId;

    private String secretKey;
}
