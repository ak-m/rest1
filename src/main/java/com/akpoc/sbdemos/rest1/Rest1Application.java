package com.akpoc.sbdemos.rest1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class Rest1Application {

	public static void main(String[] args) {
		SpringApplication.run(Rest1Application.class, args);
	}

}


@RestController
@Slf4j
class HostInfoController {
	
	@GetMapping("/hostinfo")
	public String getHostInfo() throws UnknownHostException {
		log.debug("getHostInfo()");
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm:ss a");
		String serverTime = ofPattern.format(LocalDateTime.now());
		InetAddress localHost = InetAddress.getLocalHost();
		String serverResponse = new StringBuilder().append("IP ").append(localHost.getHostAddress())
							.append("Host :").append(localHost.getHostName())
							.append("@ ").append(serverTime)
							.toString();
		log.debug("Server resposne {} ", serverResponse);
		
		return serverResponse;
	}
}