#!/usr/bin/env python3
"""
Mock AI 추천 서버
실제 AI 없이 간단한 규칙 기반으로 추천을 시뮬레이션
"""

from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import random

app = FastAPI(title="Mock AI Recommender", version="1.0.0")

class Candidate(BaseModel):
    postId: int
    price: float
    distance: float
    trust: float

class RecommendRequest(BaseModel):
    user_id: int
    money_weight: float
    distance_weight: float
    trust_weight: float
    candidates: List[Candidate]
    top_n: int

@app.post("/recommend")
def recommend(request: RecommendRequest):
    """
    간단한 가중합 점수 기반 추천
    """
    print(f"🤖 AI 추천 요청 받음:")
    print(f"   사용자: {request.user_id}")
    print(f"   후보 수: {len(request.candidates)}")
    print(f"   가중치: 금액={request.money_weight}, 거리={request.distance_weight}, 신뢰={request.trust_weight}")
    
    if not request.candidates:
        return {"ranked_post_ids": []}
    
    # 각 후보에 점수 계산
    scored_candidates = []
    for candidate in request.candidates:
        # 정규화 (0~1 범위로)
        price_score = 1.0 - min(candidate.price / 30000, 1.0)  # 30000원 기준
        distance_score = 1.0 - min(candidate.distance / 10000, 1.0)  # 10km 기준  
        trust_score = min(candidate.trust / 100, 1.0)  # 100점 기준
        
        # 가중합 계산
        total_score = (
            request.money_weight * price_score +
            request.distance_weight * distance_score + 
            request.trust_weight * trust_score
        )
        
        scored_candidates.append((candidate.postId, total_score))
        print(f"   Post {candidate.postId}: 점수={total_score:.3f} (가격={price_score:.2f}, 거리={distance_score:.2f}, 신뢰={trust_score:.2f})")
    
    # 점수 기준 정렬
    scored_candidates.sort(key=lambda x: x[1], reverse=True)
    
    # 상위 N개 반환
    recommended_ids = [post_id for post_id, score in scored_candidates[:request.top_n]]
    
    print(f"🎯 추천 결과: {recommended_ids}")
    return {"ranked_post_ids": recommended_ids}

@app.get("/")
def root():
    return {"message": "Mock AI Recommender Server", "status": "running"}

@app.get("/health")
def health():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    print("🚀 Mock AI 서버 시작 중...")
    print("📡 http://localhost:8000")
    uvicorn.run(app, host="localhost", port=8000) 