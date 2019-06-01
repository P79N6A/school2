package com.mnevent.hz.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefheUtils {

 private static final String SHARE_PREFS_NAME = "xiao";

 public static void putBoolean(Context ctx, String key, boolean value) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  pref.edit().putBoolean(key, value).commit();
 }

 public static boolean getBoolean(Context ctx, String key,
   boolean defaultValue) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  return pref.getBoolean(key, defaultValue);
 }

 public static void putString(Context ctx, String key, String value) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  pref.edit().putString(key, value).commit();
 }

 public static String getString(Context ctx, String key, String defaultValue) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  return pref.getString(key, defaultValue);
 }

 public static void putInt(Context ctx, String key, int value) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  pref.edit().putInt(key, value).commit();
 }

 public static int getInt(Context ctx, String key, int defaultValue) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
    Context.MODE_PRIVATE);

  return pref.getInt(key, defaultValue);
 }

 public static void putLong(Context ctx, String key, Long value) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
          Context.MODE_PRIVATE);

  pref.edit().putLong(key, value).commit();
 }

 public static Long getLong(Context ctx, String key, Long LongValue) {
  SharedPreferences pref = ctx.getSharedPreferences(SHARE_PREFS_NAME,
          Context.MODE_PRIVATE);

  return pref.getLong(key, LongValue);
 }
}
