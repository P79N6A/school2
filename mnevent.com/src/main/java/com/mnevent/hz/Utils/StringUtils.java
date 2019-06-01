package com.mnevent.hz.Utils;

public class StringUtils
{


	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TAG = StringUtils.class.getSimpleName();

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	//  abstract methods
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


	public static int atoi(String s, int offset)
	{
		int neg = 1;
		int n = 0;

		// check for minus prefix
		if (s.charAt(offset) == '-')
		{
			n = -1;
			offset++;
		}

		// parse number
		while (offset < s.length() && Character.isDigit(s.charAt(offset)))
		{
			n = (n * 10) + (s.charAt(offset) - '0');
			offset++;
		}

		return n * (neg);
	}

	public static String int2Str(int i)
	{
		return String.valueOf(i);
	}

	public static String float2Str(float f)
	{
		return String.valueOf(f);
	}

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

}
