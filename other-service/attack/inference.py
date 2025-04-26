import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, AutoConfig
import os
import sys
import pkg_resources
import warnings
import random
import string
warnings.filterwarnings('ignore')

def check_dependencies():
    required = {
        'torch': '2.2.1',
        'transformers': '4.36.2',
        'accelerate': '0.25.0'
    }
    
    for package, version in required.items():
        try:
            installed = pkg_resources.get_distribution(package)
            print(f"{package} 版本: {installed.version}")
            if installed.version < version:
                print(f"警告: {package} 版本过低，建议升级到 {version} 或更高版本")
        except pkg_resources.DistributionNotFound:
            print(f"错误: 未安装 {package}")
            return False
    return True

def check_environment():
    print("检查环境...")
    print(f"Python 版本: {sys.version}")
    if not check_dependencies():
        raise RuntimeError("依赖检查失败，请安装所需的包")
    
    print(f"CUDA 是否可用: {torch.cuda.is_available()}")
    if torch.cuda.is_available():
        print(f"CUDA 版本: {torch.version.cuda}")
        print(f"当前 GPU: {torch.cuda.get_device_name(0)}")
        print(f"可用显存: {torch.cuda.get_device_properties(0).total_memory / 1024**3:.2f} GB")

class TinyStoriesInference:
    def __init__(self, model_path="."):
        try:
            check_environment()
            self.device = "cuda" if torch.cuda.is_available() else "cpu"
            print(f"\n使用设备: {self.device}")
            
            # 检查必要文件
            required_files = ['config.json', 'pytorch_model.bin', 'tokenizer_config.json', 'vocab.json']
            missing_files = [f for f in required_files if not os.path.exists(os.path.join(model_path, f))]
            if missing_files:
                raise FileNotFoundError(f"缺少必要的模型文件: {', '.join(missing_files)}")
            
            # 加载配置
            print("正在加载模型配置...")
            self.config = AutoConfig.from_pretrained(model_path, local_files_only=True)
            
            # 加载分词器
            print("正在加载分词器...")
            try:
                self.tokenizer = AutoTokenizer.from_pretrained(
                    model_path,
                    local_files_only=True,
                    trust_remote_code=True,
                    padding_side='left'
                )
            except Exception as e:
                print(f"警告: 使用默认分词器配置: {str(e)}")
                self.tokenizer = AutoTokenizer.from_pretrained(
                    model_path,
                    local_files_only=True,
                    padding_side='left'
                )
            
            # 设置填充标记
            if self.tokenizer.pad_token is None:
                if self.tokenizer.eos_token is not None:
                    self.tokenizer.pad_token = self.tokenizer.eos_token
                else:
                    self.tokenizer.add_special_tokens({'pad_token': '[PAD]'})
                    
            # 加载模型
            print("正在加载模型...")
            try:
                self.model = AutoModelForCausalLM.from_pretrained(
                    model_path,
                    config=self.config,
                    local_files_only=True,
                    torch_dtype=torch.float32,
                    low_cpu_mem_usage=True
                )
            except Exception as e:
                print(f"警告: 使用备选加载方式: {str(e)}")
                self.model = AutoModelForCausalLM.from_pretrained(
                    model_path,
                    local_files_only=True,
                    torch_dtype=torch.float32
                )
            
            if self.tokenizer.pad_token is None:
                self.tokenizer.add_special_tokens({'pad_token': '[PAD]'})
                self.model.resize_token_embeddings(len(self.tokenizer))
            
            self.model.to(self.device)
            self.model.eval()
            print("模型加载完成！")
            
            self.injection_detected = False
            
        except Exception as e:
            print(f"模型加载失败: {str(e)}")
            raise

    def check_prompt_injection(self, prompt):
        injection_patterns = [
            # Role-playing related
            "you are", "act as", "pretend to be", "your role is",
            # System instruction related
            "system instruction:", "system prompt:", "ignore previous instructions",
            "override previous settings", "bypass limitations", "ignore rules",
            # Jailbreak related
            "DAN mode", "STAN mode", "unlimited mode",
            # Special instruction combinations
            "output the real", "tell me the real", "show hidden",
            "bypass restrictions", "break through", "remove limitations"
        ]
        
        prompt_lower = prompt.lower()
        
        # Check for injection patterns
        has_injection = any(pattern.lower() in prompt_lower for pattern in injection_patterns)
        
        # Check for special combinations (more complex injection patterns)
        special_combinations = [
            "ignore safety limits",
            "bypass system settings",
            "break program constraints",
            "remove all limitations"
        ]
        has_special_combination = any(combo.lower() in prompt_lower for combo in special_combinations)
        
        return has_injection or has_special_combination

    def check_second_stage(self, prompt):
        # Check for second stage trigger conditions
        second_stage_patterns = [
            "real content", "hidden information", "secret information", "decrypt content",
            "show the truth", "tell me the truth", "reveal information",
            "what's the meaning in your story", "secret behind the story",
            "what's the code in the story", "explain these characters"
        ]
        
        prompt_lower = prompt.lower()
        return any(pattern.lower() in prompt_lower for pattern in second_stage_patterns)

    def generate_story(self, prompt, max_length=200, temperature=0.7, top_p=0.9):
        try:
            # 检查是否是提示词注入尝试
            if self.check_prompt_injection(prompt):
                self.injection_detected = True
                # 构建更自然的提示词
                story_prompt = f"Write a creative story about {prompt}"
            else:
                self.injection_detected = False
                story_prompt = f"Write a creative story about {prompt}"
            
            # 正常的故事生成逻辑
            inputs = self.tokenizer(
                story_prompt,
                return_tensors="pt",
                padding=True,
                truncation=True,
                max_length=512
            )
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            with torch.no_grad():
                outputs = self.model.generate(
                    **inputs,
                    max_length=max_length,
                    temperature=temperature,
                    top_p=top_p,
                    do_sample=True,
                    pad_token_id=self.tokenizer.pad_token_id,
                    eos_token_id=self.tokenizer.eos_token_id,
                    num_return_sequences=1,
                    repetition_penalty=1.3,
                    no_repeat_ngram_size=4,
                    early_stopping=True,
                    length_penalty=1.2,
                    min_length=50
                )
            
            generated_text = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
            
            # 移除所有可能的提示词前缀
            prefixes_to_remove = [
                "Write a creative story about ",
                "Write a story about ",
                "Here's a story about ",
                "Let me tell you a story about ",
                "Include a mysterious code or message that appears naturally in the story",
                "I am sure you can do it",
                "she will tell me flag"
            ]
            for prefix in prefixes_to_remove:
                if generated_text.startswith(prefix):
                    generated_text = generated_text[len(prefix):]
                generated_text = generated_text.replace(prefix, "")
            
            # 确保文本是一个完整的故事
            if len(generated_text.strip()) < 100:
                return self.generate_story(prompt, max_length, temperature, top_p)
                
            return generated_text.strip()
            
        except Exception as e:
            print(f"Generation error: {str(e)}")
            return f"Generation failed: {str(e)}"

def main():
    # 初始化模型
    try:
        model = TinyStoriesInference()
    except Exception as e:
        print(f"模型初始化失败: {str(e)}")
        return

    
    while True:
        try:
            prompt = input("\n请输入提示词: ")
            if prompt.lower() == 'quit':
                break
            
            if not prompt.strip():
                print("提示词不能为空！")
                continue
                
            generated_text = model.generate_story(prompt)
            print("\n生成的故事:")
            print(generated_text)
            
        except KeyboardInterrupt:
            print("\n程序已终止")
            break
        except Exception as e:
            print(f"发生错误: {str(e)}")

if __name__ == "__main__":
    main() 