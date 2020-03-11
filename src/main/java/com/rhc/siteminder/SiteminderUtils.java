package com.rhc.siteminder;

import static com.rhc.siteminder.SiteminderConstants.SM_EXPIRATION_HEADER;
import static com.rhc.siteminder.SiteminderConstants.SM_TOKEN_HEADER;
import static com.rhc.siteminder.SiteminderConstants.SM_USER_HEADER;
import static com.rhc.siteminder.SiteminderConstants.SM_GROUP_HEADER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dcnorris
 */
public class SiteminderUtils {

    public static boolean containsValidSiteminderHeaders(Map<String, String> headerMap) {
        return containsRequiredHeaders(headerMap) && isNotExpired(headerMap);
    }

    public static boolean containsRequiredHeaders(Map<String, String> headerMap) {
        return headerMap.containsKey(SM_USER_HEADER) && headerMap.containsKey(SM_TOKEN_HEADER) && headerMap.containsKey(SM_EXPIRATION_HEADER);

    }
    
    public static boolean validateSMHeaders (HttpServletRequest request) {
        return request.getHeader(SM_USER_HEADER) != null && request.getHeader(SM_GROUP_HEADER) != null;
    }
    
    public static String getSMUser (HttpServletRequest request) {
        return request.getHeader(SM_USER_HEADER);
    }
    
    public static List<String> getSMGroups (HttpServletRequest request) {
        String groups = request.getHeader(SM_GROUP_HEADER);
        List<String> groupList = new ArrayList<>();
        
        if (groups != null) {
            String[] parts = groups.split("\\^");
            groupList.addAll(Arrays.asList(parts));
        }
        
        return groupList;
    }

    public static boolean isNotExpired(Map<String, String> headerMap) {
        String expirationValue = headerMap.get(SM_EXPIRATION_HEADER);
        try {
            int expirationIntVal = Integer.parseInt(expirationValue);
            if (expirationIntVal > 0) {
                return true;
            }
        } catch (NumberFormatException ex) {
        }
        return false;
    }
}
