package com.javis.learn_hub.support.infrastructure;

import com.javis.learn_hub.support.domain.OauthProvider;
import com.javis.learn_hub.support.domain.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class TestOAuthProvider implements OauthProvider {

    private static final String PROVIDER_NAME = "test";

    @Override
    public UserProfile getUserProfile(String accessToken) {
        return () -> Long.parseLong(accessToken);
    }

    @Override
    public boolean is(String name) {
        return PROVIDER_NAME.equals(name);
    }
}
