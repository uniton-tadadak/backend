package com.unithon.tadadak.recommend.dto;

import java.util.List;

public record RecommendResponse(
        java.util.List<Long> ranked_post_ids
) {}
