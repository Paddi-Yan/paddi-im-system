package com.paddi.service.module.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:30:27
 */
@Data
public class ImportUserResponse {

    private List<String> successId;

    private List<String> errorId;
}
