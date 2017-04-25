package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.Map;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}

	public int getRegistrationState(String id) {
		// TODO Auto-generated method stub
		info("Called getRegistrationState with %s",id);
		return 0;
	}

	public int getRegistrationStates(Map<String, String> params) {
		// TODO Auto-generated method stub
		info("Called getRegistrationStates with %s", params);
		return 0;
	}	
}
