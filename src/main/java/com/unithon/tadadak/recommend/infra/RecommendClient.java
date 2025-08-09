package com.unithon.tadadak.recommend.infra;

import com.unithon.tadadak.recommend.dto.RecommendRequest;
import com.unithon.tadadak.recommend.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;



import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendClient {

    private final RestTemplate restTemplate;

    @Value("${ai.recommender.base-url}")
    private String baseUrl;

    public List<Long> rank(RecommendRequest req) {
        String url = baseUrl + "/recommend";
        RecommendResponse resp = restTemplate.postForObject(url, req, RecommendResponse.class);
        if (resp == null || resp.ranked_post_ids() == null) {
            return List.of();
        }
        return resp.ranked_post_ids();
    }
}
