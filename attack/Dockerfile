# 使用 Python 3.12 作为基础镜像
FROM python:3.12-slim

# 设置工作目录
WORKDIR /app

# 复制依赖文件
COPY requirements.txt .

# 安装依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码
COPY . .

# 暴露端口
EXPOSE 11451

# 设置环境变量
ENV FLASK_APP=server.py
ENV FLASK_ENV=production

# 启动命令
CMD ["python", "server.py"] 