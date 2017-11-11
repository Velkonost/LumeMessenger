package com.velkonost.lume.vkontakte;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;

/**
 * @author Velkonost
 */

public class VkApiHelper {


    public static VKRequest getAuthUserData(String userId) {
        return VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, userId, FIELDS, PHOTO_50));
    }
}
