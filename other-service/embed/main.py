# embedding_server.py
from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModel
import torch

app = Flask(__name__)

# 加载本地模型
tokenizer = AutoTokenizer.from_pretrained("BAAI/bge-small-en-v1.5")
model = AutoModel.from_pretrained("BAAI/bge-small-en-v1.5")

@app.route("/embed", methods=["POST"])
def embed():
    data = request.json
    text = data['text']
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    with torch.no_grad():
        embeddings = model(**inputs).last_hidden_state[:,0,:]
    embeddings = embeddings.squeeze(0).tolist()
    return jsonify({"embedding": embeddings})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=20000)
