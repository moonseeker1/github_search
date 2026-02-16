"""
BGE-M3 Embedding Service
Provides text embedding API using BGE-M3 model
"""

from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
import torch
from sentence_transformers import SentenceTransformer
import uvicorn

app = FastAPI(title="BGE-M3 Embedding Service")

# Global model instance
model = None

class EmbeddingRequest(BaseModel):
    texts: List[str]
    normalize: bool = True

class EmbeddingResponse(BaseModel):
    embeddings: List[List[float]]
    dimension: int
    model: str

class RerankRequest(BaseModel):
    query: str
    documents: List[str]

class RerankResponse(BaseModel):
    scores: List[float]
    model: str

@app.on_event("startup")
async def load_model():
    global model
    print("Loading BGE-M3 model...")
    model = SentenceTransformer('BAAI/bge-m3', trust_remote_code=True)
    print("BGE-M3 model loaded successfully!")

@app.post("/embedding", response_model=EmbeddingResponse)
async def get_embeddings(request: EmbeddingRequest):
    """Get embeddings for texts"""
    global model

    embeddings = model.encode(
        request.texts,
        normalize_embeddings=request.normalize,
        convert_to_numpy=True
    )

    return EmbeddingResponse(
        embeddings=embeddings.tolist(),
        dimension=embeddings.shape[1],
        model="BAAI/bge-m3"
    )

@app.get("/health")
async def health():
    return {"status": "healthy", "model": "BAAI/bge-m3"}

@app.get("/")
async def root():
    return {
        "service": "BGE-M3 Embedding Service",
        "model": "BAAI/bge-m3",
        "endpoints": ["/embedding", "/health"]
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
