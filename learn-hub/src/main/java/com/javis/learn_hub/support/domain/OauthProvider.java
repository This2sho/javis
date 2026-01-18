package com.javis.learn_hub.support.domain;

public interface OauthProvider {

    UserProfile getUserProfile(String accessToken);

    boolean is(String name);
}
