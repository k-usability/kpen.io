package io.kpen.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
	private static final Dotenv env = Dotenv.configure().directory("./").ignoreIfMissing().load();
	public static synchronized String get (String variable) {
		return env.get(variable);
	}
}