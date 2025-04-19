import multiprocessing

# 绑定的地址和端口
bind = "0.0.0.0:11451"

# 工作进程数，建议设置为 CPU 核心数的 2-4 倍
workers = multiprocessing.cpu_count() * 2

# 使用 gevent worker
worker_class = "geventwebsocket.gunicorn.workers.GeventWebSocketWorker"

# 每个工作进程的线程数
threads = 4

# 超时设置
timeout = 120

# 最大请求数，超过后工作进程会重启
max_requests = 1000
max_requests_jitter = 50

# 日志设置
accesslog = "access.log"
errorlog = "error.log"
loglevel = "info"

# 进程名称
proc_name = "story_generator"

# 预加载应用
preload_app = True

# 守护进程模式
daemon = False

# 重启时间
graceful_timeout = 30

# 保持连接时间
keepalive = 2 