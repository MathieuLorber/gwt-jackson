package com.github.nmorel.gwtjackson.hello.shared;

public class GreetingResponse {

	private String greeting;

	private String serverInfo;

	private String userAgent;

	private HasCode code;

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public String getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(String serverInfo) {
		this.serverInfo = serverInfo;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public HasCode getCode() {
		return code;
	}

	public void setCode(HasCode code) {
		this.code = code;
	}

}
