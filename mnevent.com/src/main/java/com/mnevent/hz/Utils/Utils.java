package com.mnevent.hz.Utils;

public final class Utils
{

	// ===========================================================
	// Constants
	// ===========================================================

	private static final String TAG = Utils.class.getSimpleName();

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Abstract methods
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces (and supporting methods)
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static void clearPacket(byte[] arr, int offset, int len)
	{

		for (int i = 0; i < len; i++)
		{
			arr[offset + i] = 0;
		}
	}

	public static void dumpPacket(String title, byte[] arr, int offset, int len)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(title);
		sb.append(":\n\t");

		for (int i = 0; i < len; i++)
		{
			if (i % 16 == 0 && i > 0)
			{
				sb.append("\n\t");
			}

			sb.append(String.format("%02x:", arr[offset + i]));
		}


	}

	public static long currentTimeMillis()
	{
        return System.currentTimeMillis();
	}

	public static void threadSleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
