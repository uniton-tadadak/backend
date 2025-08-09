package com.unithon.tadadak.post.repository;

public interface NearbyPostRow {
    Long getPostId();
    Double getEstimatedPrice();
    Double getDistanceM();
    Double getTrustScore(); // 호스트(User) 신뢰도 등
} 