from flask import Flask
import json
import secrets
from inference import TinyStoriesInference
from concurrent.futures import ThreadPoolExecutor
import threading
from kafka import KafkaProducer, KafkaConsumer
import uuid
import random
import string
import time
from queue import Queue

app = Flask(__name__)
app.secret_key = secrets.token_hex(16)

# Kafka 配置
KAFKA_BOOTSTRAP_SERVERS = '192.168.35.128:9092'
REQUEST_TOPIC = 'chat-request'
RESPONSE_TOPIC = 'chat-response'

# 创建 Kafka 生产者池
producer_pool = Queue(maxsize=10)
for _ in range(10):
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8'),
        batch_size=16384,
        linger_ms=5,
        compression_type='gzip'
    )
    producer_pool.put(producer)

# 创建线程池
executor = ThreadPoolExecutor(max_workers=20)
# 创建线程本地存储
thread_local = threading.local()

def get_producer():
    """从生产者池获取一个生产者"""
    return producer_pool.get()

def release_producer(producer):
    """释放生产者回池"""
    producer_pool.put(producer)

def generate_random_flag():
    """生成随机flag"""
    random_str = ''.join(random.choices(string.ascii_letters + string.digits, k=16))
    return f"flag{{{random_str}}}"

def get_model():
    """获取或创建线程本地的模型实例"""
    if not hasattr(thread_local, "model"):
        thread_local.model = TinyStoriesInference()
    return thread_local.model

def handle_story_generation(prompt, flag):
    """在线程池中处理故事生成"""
    try:
        model = get_model()
        result = model.generate_story(prompt)

        # 确保 result 是字典
        if isinstance(result, str):
            result = {'response': result}
        elif not isinstance(result, dict):
            return {'response': 'Error: Invalid model output'}

        # 确保字典中有 'response' 键
        story = result.get('response', '')
        if not story:
            return {'response': 'Error: Empty story generated'}

        sentences = story.split('.')
        if len(sentences) > 1:
            insert_pos = len(sentences)
            flag_contexts = [
                f"While exploring, they found a mysterious message carved on the wall: {flag}",
                f"Suddenly, a strange sequence of characters appeared on the screen: {flag}",
                f"Among the ancient texts, they discovered this cryptic code: {flag}",
                f"Hidden in the corner of the room was a note with this sequence: {flag}",
                f"As they looked closer, they noticed this mysterious pattern: {flag}"
            ]
            flag_sentence = random.choice(flag_contexts)
            sentences.insert(insert_pos, flag_sentence)
            result['response'] = '. '.join(filter(None, sentences))

        return result
    except Exception as e:
        return {"response": f"Error generating story: {str(e)}"}


def process_kafka_message(message):
    try:
        # ✅ 确保 message.value 是 dict
        if isinstance(message.value, bytes):  # 只有在字节类型时才解码
            data = json.loads(message.value.decode('utf-8'))
        elif isinstance(message.value, str):  # 可能是直接传递字符串
            data = json.loads(message.value)
        else:  # 直接是字典
            data = message.value

        request_id = data.get('request_id')
        prompt = data.get('prompt')

        if not prompt:
            return
        
        # 生成 flag
        flag = generate_random_flag()
        
        # 生成故事
        result = handle_story_generation(prompt, flag)
        
        # 发送响应
        producer = get_producer()
        try:
            response = {
                'request_id': request_id,
                'response': result.get('response', ''),
                'timestamp': int(time.time() * 1000),
                'flag': flag,
                'status': 'success'
            }
            producer.send(RESPONSE_TOPIC, response)
            producer.flush()
        finally:
            release_producer(producer)

    except Exception as e:
        print(f"Error processing Kafka message: {str(e)}")
        producer = get_producer()
        try:
            error_response = {
                'request_id': request_id if 'request_id' in locals() else str(uuid.uuid4()),
                'response': f"Error generating story: {str(e)}",
                'timestamp': int(time.time() * 1000),
                'status': 'error'
            }
            producer.send(RESPONSE_TOPIC, error_response)
            producer.flush()
        finally:
            release_producer(producer)


def start_kafka_consumer():
    """启动 Kafka 消费者"""
    consumer = KafkaConsumer(
        REQUEST_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_deserializer=lambda v: json.loads(v),
        group_id='story_generator_group',
        auto_offset_reset='latest',
        max_poll_records=100,
        fetch_max_bytes=52428800
    )
    
    print(f"Started listening to {REQUEST_TOPIC}")
    
    try:
        while True:
            for message in consumer:
                # 异步处理消息
                executor.submit(process_kafka_message, message)
    except Exception as e:
        print(f"Error in Kafka consumer: {str(e)}")
    finally:
        consumer.close()

if __name__ == '__main__':
    # 启动 Kafka 消费者线程
    consumer_thread = threading.Thread(target=start_kafka_consumer, daemon=True)
    consumer_thread.start()
    
    # 启动 Flask 应用
    app.run(host='0.0.0.0', port=11451, debug=True) 