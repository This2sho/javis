package com.javis.learn_hub.support.domain;

public class MemberId {

    private static final MemberId guest = new MemberId(-1L);

    private final Long id;

    private MemberId(Long id) {
        this.id = id;
    }

    public static MemberId guest() {
        return guest;
    }

    public static MemberId from(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalStateException("id is invalid");
        }
        return new  MemberId(id);
    }

    public Long getId() {
        return id;
    }
}
