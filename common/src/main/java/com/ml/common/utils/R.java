package com.ml.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.io.Serializable;

public class R<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private int code;
	private String msg;
	private T data;

	public R() {
		this.code = 0;
		this.msg = "success";
	}

	public R(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public static <T> R<T> ok() {
		return new R<>();
	}

	public static <T> R<T> ok(String msg) {
		return new R<>(0, msg, null);
	}

	public static <T> R<T> ok(T data) {
		return new R<>(0, "success", data);
	}

	public static <T> R<T> ok(String msg, T data) {
		return new R<>(0, msg, data);
	}

	public static <T> R<T> error() {
		return new R<>(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员", null);
	}

	public static <T> R<T> error(int code, String msg) {
		return new R<>(code, msg, null);
	}

	public R<T> setData(T data) {
		this.data = data;
		return this;
	}

	public <V> V getData(TypeReference<V> typeReference) {
		String json = JSON.toJSONString(this.data);
		return JSON.parseObject(json, typeReference);
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public T getData() {
		return data;
	}
}
