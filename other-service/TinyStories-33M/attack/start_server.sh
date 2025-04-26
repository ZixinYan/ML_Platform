#!/bin/bash

# 启动服务器
gunicorn -k geventwebsocket.gunicorn.workers.GeventWebSocketWorker \
         -w 4 \
         -b 0.0.0.0:11451 \
         --timeout 120 \
         --max-requests 1000 \
         --max-requests-jitter 50 \
         --access-logfile access.log \
         --error-logfile error.log \
         --log-level info \
         server:app 