package com.cambiahealth.portal.dbcleanup.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import java.lang.reflect.Method;
public class StagingAdvicesUtil {

	public static boolean disableStagingAdvices() {
		boolean isEnabled = true;

		try {
			isEnabled = (Boolean)_stagingAdvicesIsEnabled.invoke(null);

			_stagingAdvicesSetEnabled.invoke(null, false);

			_log.info(">>> Disabled staging advices");
		}
		catch (Exception e) {
			_log.error(">>> Error disabling staging advices", e);
		}

		return isEnabled;
	}

	public static void resetStagingAdvices(boolean originalValue) {
		try {
			_stagingAdvicesSetEnabled.invoke(null, originalValue);

			_log.info(">>> Reset staging advices");
		}
		catch (Exception e) {
			_log.error(">>> Error resetting staging advices", e);
		}
	}

	private StagingAdvicesUtil() {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		StagingAdvicesUtil.class);

	private static final Method _stagingAdvicesIsEnabled;

	private static final Method _stagingAdvicesSetEnabled;

	static {
		try {
			Class<?> stagingAdvicesThreadLocalClass =
				PortalClassLoaderUtil.getClassLoader().loadClass(
					"com.liferay.portal.staging.StagingAdvicesThreadLocal");

			_stagingAdvicesIsEnabled = stagingAdvicesThreadLocalClass.getMethod(
				"isEnabled");

			_stagingAdvicesSetEnabled =
				stagingAdvicesThreadLocalClass.getMethod(
					"setEnabled", Boolean.TYPE);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}