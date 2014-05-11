package com.whr.taskmanager.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskManagerUtil {

	public static String changeToDateAndTimeFormat(long time) {
		Date tmp = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm",
				Locale.CHINA);
		return sdf.format(tmp);
	}

	/**
	 * 改变成日期
	 * 
	 * @param time
	 * @return
	 */
	public static String changeToDateFormat(long time) {
		Date tmp = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
		return sdf.format(tmp);
	}

	/**
	 * @param msg
	 * @return
	 */
	public static String changeToDateFormat(String msg) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd", Locale.CHINA);
			ParsePosition pos = new ParsePosition(0);
			Date tmp = formatter.parse(msg, pos);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日",
					Locale.CHINA);
			return sdf.format(tmp);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * 改变成时间制度
	 * 
	 * @param time
	 * @return
	 */
	public static String changeToTimeFormat(long time) {
		Date tmp = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
		return sdf.format(tmp);
	}

	/**
	 * @param msg
	 * @return
	 */
	public static String changeToTimeFormat(String msg) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"HH:mm:ss", Locale.CHINA);
			ParsePosition pos = new ParsePosition(0);
			Date tmp = formatter.parse(msg, pos);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
			return sdf.format(tmp);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * 转换为System.currentTime
	 * 
	 * @param time
	 * @return
	 */
	public static long changeToLongTime(String dateTime) {
		long tmp = 0L;
		try {
			DateFormat df2 = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
			Date date = df2.parse(dateTime);
			tmp = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return tmp;
	}

	static double DEF_PI = 3.14159265359; // PI
	static double DEF_2PI = 6.28318530712; // 2*PI
	static double DEF_PI180 = 0.01745329252; // PI/180.0
	static double DEF_R = 6370693.5; // radius of earth

	/**
	 * 百度地图距离计算
	 * 
	 * @param lon1
	 * @param lat1
	 * @param lon2
	 * @param lat2
	 * @return
	 */
	public static double GetShortDistance(double lon1, double lat1,
			double lon2, double lat2) {
		double ew1, ns1, ew2, ns2;
		double dx, dy, dew;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 经度差
		dew = ew1 - ew2;
		// 若跨东经和西经180 度，进行调整
		if (dew > DEF_PI)
			dew = DEF_2PI - dew;
		else if (dew < -DEF_PI)
			dew = DEF_2PI + dew;
		dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
		dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
		// 勾股定理求斜边长
		distance = Math.sqrt(dx * dx + dy * dy);
		return distance;
	}
}
