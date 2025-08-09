package com.unithon.tadadak.recommend.dto;
import java.util.List;

public record Candidate (
    Long postId,
    double price,
    double distance,
    double trust
) {}
