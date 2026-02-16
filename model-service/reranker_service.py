"""
Reranker Service
Provides document reranking API using BGE-Reranker model
"""

from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import torch
from sentence_transformers import CrossEncoder
import uvicorn

app = FastAPI(title="BGE Reranker Service")

# Global model instance
model = None

class RerankRequest(BaseModel):
    query: str
    documents: List[str]
    top_k: int = None

class RerankResult(BaseModel):
    index: int
    score: float
    document: str

class RerankResponse(BaseModel):
    results: List[RerankResult]
    model: str

@app.on_event("startup")
async def load_model():
    global model
    print("Loading BGE-Reranker model...")
    model = CrossEncoder('BAAI/bge-reranker-v2-m3', max_length=512)
    print("BGE-Reranker model loaded successfully!")

@app.post("/rerank", response_model=RerankResponse)
async def rerank(request: RerankRequest):
    """Rerank documents based on query relevance"""
    global model

    # Create query-document pairs
    pairs = [[request.query, doc] for doc in request.documents]

    # Get relevance scores
    scores = model.predict(pairs)

    # Create results with indices
    results = [
        RerankResult(index=i, score=float(scores[i]), document=request.documents[i])
        for i in range(len(request.documents))
    ]

    # Sort by score descending
    results.sort(key=lambda x: x.score, reverse=True)

    # Limit to top_k if specified
    if request.top_k is not None:
        results = results[:request.top_k]

    return RerankResponse(
        results=results,
        model="BAAI/bge-reranker-v2-m3"
    )

@app.post("/rerank/scores")
async def get_scores(request: RerankRequest):
    """Get raw relevance scores for documents"""
    global model

    pairs = [[request.query, doc] for doc in request.documents]
    scores = model.predict(pairs).tolist()

    return {
        "scores": scores,
        "model": "BAAI/bge-reranker-v2-m3"
    }

@app.get("/health")
async def health():
    return {"status": "healthy", "model": "BAAI/bge-reranker-v2-m3"}

@app.get("/")
async def root():
    return {
        "service": "BGE Reranker Service",
        "model": "BAAI/bge-reranker-v2-m3",
        "endpoints": ["/rerank", "/rerank/scores", "/health"]
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
