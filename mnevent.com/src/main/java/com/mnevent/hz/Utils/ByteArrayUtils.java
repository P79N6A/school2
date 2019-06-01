package com.mnevent.hz.Utils;

public final class ByteArrayUtils
{


	// ===========================================================
	// Constants
	// ===========================================================

	public static final String TAG = ByteArrayUtils.class.getSimpleName();

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

	public static int byteArrToInteger(byte[] arr, int offset)
	{
		int intValue = 0;

		intValue = (((int)arr[offset + 0]) & 0xff);
		intValue = intValue + (((int)(arr[offset + 1]) & 0xff) << 8);
		intValue = intValue + (((int)(arr[offset + 2]) & 0xff) << 16);
		intValue = intValue + (((int)(arr[offset + 3]) & 0xff) << 24);

		return intValue;
	}

	public static short byteArrToShort(byte[] arr, int offset)
	{

		short shortValue = 0;

		shortValue = (short)(arr[offset + 0] & 0xff);
		shortValue = (short)(shortValue + (((int)(arr[offset + 1]) & 0xff) << 8));

		return shortValue;
	}

	public static byte byteArrToByte(byte[] arr, int offset)
	{
		return arr[offset];
	}

	public static int byteToByteArray(byte[] arr, int offset, byte value)
	{
		arr[offset + 0] = (byte)(value);

		return 1;
	}

	public static int shortToByteArray(byte[] arr, int offset, short value)
	{
		arr[offset + 0] = (byte)(value);
		arr[offset + 1] = (byte)(value >>> 8);

		return 2;
	}

	public static int intToByteArray(byte[] arr, int offset, int value)
	{
		arr[offset + 0] = (byte)(value >> 0);
		arr[offset + 1] = (byte)(value >> 8);
		arr[offset + 2] = (byte)(value >> 16);
		arr[offset + 3] = (byte)(value >> 24);

		return 4;
	}

	public static int stringToByteArray(byte[] arr, int offset, String str, int max_len)
	{
		byte[] str_byte_arr = null;

		str_byte_arr = str.getBytes();
			max_len = str_byte_arr.length < max_len ? str_byte_arr.length : max_len;

			for (int i = 0; i < str_byte_arr.length; i++)
			{
				arr[offset++] = str_byte_arr[i];
			}

		return str_byte_arr.length;
	}

	public static String byteArrToString(byte[] arr, int offset, int max_len)
	{
		int str_length = max_len;

		for (int i = offset; i < offset + str_length; i++)
		{
			if (arr[i] == 0 || arr[i] == -1)
			{
				str_length = i;
				break;
			}
		}
		return new String(arr, offset, str_length);
	}

	public static int subByteArrToByteArray(byte[] arr, int offset, byte[] sub_arr)
	{
		try {
			System.arraycopy(sub_arr, 0, arr, offset, sub_arr.length);
		}
		catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
		}


		return sub_arr.length;
	}

	public static int byteArrToSubByteArray(byte[] arr, int offset, byte[] sub_arr)
	{
		try
		{
			System.arraycopy(arr, offset, sub_arr, 0, sub_arr.length);
		}
		catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
		}

		return sub_arr.length;
	}

}
