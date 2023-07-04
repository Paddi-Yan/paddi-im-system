package com.paddi.common.util;


import com.paddi.common.exception.BadRequestException;

import java.util.List;
import java.util.Objects;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月16日 16:52:33
 */
public class PageUtils {

    /**
     * 泛型方法 进行结果的分页
     * 当pageNum*pageSize>result.size那么就取result的最后一页数据
     * 否则就取相应页的数据
     *
     * @param result
     * @param pageNum
     * @param pageSize
     * @return
     */
    public static <T> List<T> pageResult(List<T> result, Integer pageNum, Integer pageSize) {
        if (Objects.isNull(result) || result.size() == 0) {
            return result;
        }
        int maxSize = result.size();
        if (maxSize < pageNum * pageSize+pageSize) {
            int maxPage = maxSize / pageSize;
            return result.subList(maxPage * pageSize, result.size());
        }
        return result.subList(pageNum * pageSize, (pageNum + 1) * pageSize);
    }

    /**
     * 分页参数校验
     *
     * @param pageNumStr
     * @param pageSizeStr
     */
    public static void pageCheck(String pageNumStr, String pageSizeStr) {

        try {
            int pageNum = Integer.parseInt(pageNumStr);
            int pageSize = Integer.parseInt(pageSizeStr);
            if (pageSize <= 0 || pageNum < 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new BadRequestException("分页参数错误");
        }
    }

    public static void pageCheck(Integer pageNum, Integer pageSize) {
        try {
            if (pageSize <= 0 || pageNum < 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new BadRequestException("分页参数错误");
        }
    }

}
