package com.mnevent.hz.Utils;

public final class Log
{
	private static java.io.OutputStreamWriter w;

	private static long m_prev_log_ts = Utils.currentTimeMillis();

	public static void initialize()
	{
	}

	public static void close()
	{
	}

	public static void d(String TAG, String message)
	{
		long ts = Utils.currentTimeMillis();

		String f_msg = String.format("(+%dms) %s: %s", (ts - m_prev_log_ts), TAG, message);
		System.out.println(f_msg);

		m_prev_log_ts = ts;
	}
}
