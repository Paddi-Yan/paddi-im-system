package com.paddi.common.utils;


import com.paddi.common.enums.BaseErrorCode;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.loadbalance.RouteInfo;

/**
 *
 * @since JDK 1.8
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String address){
        try {
            String[] serverInfo = address.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
