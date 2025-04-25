import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

# 设置设备
device = 'cuda' if torch.cuda.is_available() else 'cpu'

# 加载分词器
tokenizer = AutoTokenizer.from_pretrained('EleutherAI/gpt-neo-125M')

# 加载模型
model = AutoModelForCausalLM.from_pretrained('pytorch_model.bin')
model.to(device)
model.eval()
