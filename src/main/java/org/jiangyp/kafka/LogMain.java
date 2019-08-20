package org.jiangyp.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMain {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogMain.class);

	public static void main(String[] args) {
		LOGGER.info("test info");
		LOGGER.warn("test warn");
		LOGGER.error("test error");
		LOGGER.debug("test debug");
	}
}
