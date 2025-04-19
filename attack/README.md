---
datasets:
- roneneldan/TinyStories
---
Model trained on the TinyStories Dataset, see https://arxiv.org/abs/2305.07759

Based on GPT-Neo architecture.

License: mit

---
hyperparams used to train this model:

lr = 5e-4,
lr_schedule = constant, 
wd=0.1,
adam_beta1=0.9, adam_beta2 = 0.95,
context_length=512,
batch_size=80,
gradient_accumulation_steps=16

------ EXAMPLE USAGE ---

from transformers import AutoModelForCausalLM, AutoTokenizer, GenerationConfig

model = AutoModelForCausalLM.from_pretrained('roneneldan/TinyStories-33M')

tokenizer = AutoTokenizer.from_pretrained("EleutherAI/gpt-neo-125M")

prompt = "Once upon a time there was"

input_ids = tokenizer.encode(prompt, return_tensors="pt")

# Generate completion
output = model.generate(input_ids, max_length = 1000, num_beams=1)

# Decode the completion
output_text = tokenizer.decode(output[0], skip_special_tokens=True)

# Print the generated text
print(output_text)