package com.unithon.tadadak.config;

import com.unithon.tadadak.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final PostRepository postRepository;

    @Scheduled(fixedRate = 60000) // 60초마다 실행
    public void expireOldPosts() {
        LocalDateTime now = LocalDateTime.now();

        postRepository.findAll().forEach(post -> {
            if (post.getStatus().equals("OPEN") && post.getDepartureTime().isBefore(now)) {
                post.setStatus("EXPIRED");
                postRepository.save(post);
            }
        });
    }
}
