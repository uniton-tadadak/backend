package com.unithon.tadadak.recommend.dto;
import java.util.List;

public record RecommendRequest (
        Long user_id,
        double money_weight,
        double distance_weight,
        double trust_weight,
        List<Candidate> candidates,
        int top_n
){ }
