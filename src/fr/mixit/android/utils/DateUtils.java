/*
 * Copyright 2011 - 2013 Mathieu Calba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mixit.android.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import fr.mixit.android_2012.R;


public class DateUtils {

	public static long parse(int hour, int minute) {
		final Calendar cal = GregorianCalendar.getInstance(Locale.FRANCE);
		cal.set(2012, 04, 26, hour, minute);
		return cal.getTimeInMillis();
	}

	private static final SimpleDateFormat sParserSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());

	public static long parseISO8601(String str) {
		synchronized (sParserSdf) {
			try {
				final Date date = sParserSdf.parse(str);
				return date.getTime();
			} catch (final ParseException e) {
				Log.e("DateUtils", "Impossible to parse " + str, e);
			}
		}
		return 0;
	}

	private static final DateFormat sDateFormatSdf = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
	private static final DateFormat sTimeFormatSdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

	public static String formatSessionTime(Context ctx, long start, long end) {
		return formatSessionTime(ctx, start, end, null);
	}

	public static String formatSessionTime(Context ctx, long start, long end, String room) {
		if (ctx == null) {
			return null;
		}

		if (start == 0 && end == 0) {
			return ctx.getResources().getString(R.string.session_day_time_room_full, room);
		} else {
			final String dayStart = sDateFormatSdf.format(new Date(start));
			// final String dayEnd = sDateFormatSdf.format(new Date(end));
			final String timeStart = sTimeFormatSdf.format(new Date(start));
			final String timeEnd = sTimeFormatSdf.format(new Date(end));

			// if (dayEnd.equalsIgnoreCase(dayStart)) {
			if (TextUtils.isEmpty(room)) {
				return ctx.getResources().getString(R.string.session_day_time, dayStart, timeStart, timeEnd);
			} else {
				return ctx.getResources().getString(R.string.session_day_time_room, dayStart, timeStart, timeEnd, room);
			}
			// } else {
			// return ctx.getResources().getString(R.string.session_day_time_room, dayStart + " & " + dayEnd, timeStart, timeEnd, room);
			// }
		}
	}

	public static String formatPlanningSessionTime(Context ctx, long start, long end) {
		return formatPlanningSessionTime(ctx, start, end, null);
	}

	public static String formatPlanningSessionTime(Context ctx, long start, long end, String room) {
		if (ctx == null) {
			return null;
		}

		if (start == 0 && end == 0) {
			return ctx.getResources().getString(R.string.session_day_time_room_full, room);
		} else {
			final String timeStart = sTimeFormatSdf.format(new Date(start));
			final String timeEnd = sTimeFormatSdf.format(new Date(end));

			// if (dayEnd.equalsIgnoreCase(dayStart)) {
			if (TextUtils.isEmpty(room)) {
				return ctx.getResources().getString(R.string.session_time, timeStart, timeEnd);
			} else {
				return ctx.getResources().getString(R.string.session_time_room, timeStart, timeEnd, room);
			}
			// } else {
			// return ctx.getResources().getString(R.string.session_day_time_room, dayStart + " & " + dayEnd, timeStart, timeEnd, room);
			// }
		}
	}

	public static String formatSlotTime(Context ctx, long start, long end) {
		if (ctx == null) {
			return null;
		}

		if (start == 0 && end == 0) {
			return ctx.getResources().getString(R.string.planning_slot_pager_header_default);
		} else {
			final String timeStart = sTimeFormatSdf.format(new Date(start));
			final String timeEnd = sTimeFormatSdf.format(new Date(end));

			return ctx.getResources().getString(R.string.planning_slot_pager_header, timeStart, timeEnd);
		}
	}

	public static String formatDayTime(Context ctx, long start, long end) {
		if (ctx == null) {
			return null;
		}

		final String dayStart = sDateFormatSdf.format(new Date(start));
		final String timeStart = sTimeFormatSdf.format(new Date(start));
		final String timeEnd = sTimeFormatSdf.format(new Date(end));

		return ctx.getResources().getString(R.string.planning_duplicate_session_title, dayStart, timeStart, timeEnd);
	}

	public static CharSequence formatPlanningHeader(long start) {
		return sDateFormatSdf.format(new Date(start));
	}

}
