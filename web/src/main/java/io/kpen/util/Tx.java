package io.kpen.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.sentry.Sentry;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Tx {

	public interface TxRunnable<T> {
		T handle(DSLContext ctx) throws Throwable;
	}

	private static final Logger logger = LoggerFactory.getLogger(Tx.class);

	private static final int ROW_PREFETCH = 10000;
	private static final int BATCH_VALUE = 100;

	public static <T> T run(TxRunnable<T> ctxr) {
		try {
			return runex(ctxr);
		} catch (RuntimeException t) {
			System.out.println(ExceptionUtils.getStackTrace(t));
			Sentry.capture(t);
			throw t;
		} catch (Throwable t) {
			System.out.println(ExceptionUtils.getStackTrace(t));
			Sentry.capture(t);
			throw new RuntimeException(t);
		}
	}

	private static Connection connect(String username, String password, String url) throws SQLException {
		Properties props = new java.util.Properties();
		props.put("user", username);
		props.put("password", password);
		props.put("defaultRowPrefetch", String.valueOf(ROW_PREFETCH));
		props.put("defaultBatchValue", String.valueOf(BATCH_VALUE));
		props.put("cacheServerConfiguration", "true");

		Connection conn = DriverManager.getConnection(url, props);
		conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		return conn;
	}

	public static <T> T runex(final TxRunnable<T> pass) throws Throwable {
		StopWatch runTime = new StopWatch();
		runTime.start();

		Dotenv dotenv = Dotenv.load();

		String username = dotenv.get("APP_DB_USER");
		String password = dotenv.get("APP_DB_PASS");
		String dbname = dotenv.get("APP_DB_NAME");
		String url = "jdbc:postgresql://" + dotenv.get("APP_DB_HOST") + ":" + dotenv.get("APP_DB_PORT") + "/" + dbname + "?ssl=true";

		Throwable error = null;
		Connection conn = null;
		T returnValue = null;
		try {
			conn = connect(username, password, url);
			conn.setAutoCommit(false);

			Settings normSettings = new Settings();
			normSettings.withRenderMapping(new RenderMapping().withSchemata(
					new MappedSchema()
							.withInput(dbname)
							.withOutput(dbname)));
			normSettings.setExecuteLogging(false);

			Configuration normConfig = new DefaultConfiguration().set(new DefaultConnectionProvider(conn))
					.set(normSettings).set(SQLDialect.POSTGRES_10);

			final DSLContext ctx = DSL.using(normConfig);

			StopWatch passTime = new StopWatch();
			passTime.start(); 
			try {
				returnValue = pass.handle(ctx);
				conn.commit();
			} finally {
				passTime.stop();
				logger.debug("Pass time: " + passTime.getTime()/1000.0 + "s");
			}							
		} catch (Throwable t) {
			logger.debug(ExceptionUtils.getStackTrace(t));

			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e) {
					logger.debug(ExceptionUtils.getStackTrace(e));					
				}
			}

			error = t;
		} finally {

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.debug(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		runTime.stop();
		logger.debug("Run time: " + runTime.getTime()/1000.0 + "s");
		if (error != null) throw error;

		return returnValue;
	}
}
