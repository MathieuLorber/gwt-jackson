package com.github.nmorel.gwtjackson.hello.shared;

public enum CodeEnum implements HasCode {

	TEST_CODE("test code");

	private String code;

	private CodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

}
