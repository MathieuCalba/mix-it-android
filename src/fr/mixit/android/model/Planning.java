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

package fr.mixit.android.model;

public class Planning {

	public static final long TIMESTAMP_OFFSET_DAY_ONE = 1366840800000L; // 25 à 00h
	public static final long TIMESTAMP_OFFSET_DAY_TWO = 1366927200000L; // 26 à 00h

	public static final long FIFTEEN_MINUTES = 900000L;
	public static final long TWENTY_MINUTES = 1200000L;
	public static final long TWENTY_FIVE_MINUTES = 1500000L;
	public static final long THIRTY_MINUTES = 1800000L;
	public static final long FORTY_FIVE_MINUTES = 2700000L;
	public static final long ONE_HOUR = 3600000L;
	public static final long ONE_HOUR_AND_HALF = 5400000L;
	public static final long TWO_HOUR = 7200000L;

	public static final long EIGHT_AM = 28800000L;
	public static final long NINE_AM = EIGHT_AM + ONE_HOUR;
	public static final long TEN_AM = NINE_AM + ONE_HOUR;
	public static final long ELEVEN_AM = TEN_AM + ONE_HOUR;
	public static final long TWELVE_PM = ELEVEN_AM + ONE_HOUR;
	public static final long ONE_PM = TWELVE_PM + ONE_HOUR;
	public static final long TWO_PM = ONE_PM + ONE_HOUR;
	public static final long THREE_PM = TWO_PM + ONE_HOUR;
	public static final long FOUR_PM = THREE_PM + ONE_HOUR;
	public static final long FIVE_PM = FOUR_PM + ONE_HOUR;
	public static final long SIX_PM = FIVE_PM + ONE_HOUR;
	public static final long SEVEN_PM = SIX_PM + ONE_HOUR;

	public static final long BREAKFAST_START = EIGHT_AM;
	public static final long WELCOME_START = EIGHT_AM + FORTY_FIVE_MINUTES;
	public static final long KEYNOTE_MORNING_START = NINE_AM;
	public static final long KEYNOTE_SLOT_LENGTH = TWENTY_MINUTES;
	public static final long KEYNOTE_LONG_SLOT_LENGTH = THIRTY_MINUTES;
	public static final long PITCH_START = NINE_AM + TWENTY_MINUTES;
	public static final long TALKS_MORNING_START = NINE_AM + FORTY_FIVE_MINUTES;
	public static final long TALKS_SLOT_LENGTH = ONE_HOUR + THIRTY_MINUTES;
	public static final long LIGTHNING_TALKS_START = ONE_PM;
	public static final long LIGTHNING_TALKS_SLOT_LENGTH = THIRTY_MINUTES;
	public static final long BREAK_SLOT_LENGTH = THIRTY_MINUTES;
	public static final long TALKS_AFTERNOON_START = ONE_PM;

	public static final long[] SLOTS_LENGTH = { //
		ONE_HOUR + FORTY_FIVE_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + FORTY_FIVE_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + FORTY_FIVE_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + FORTY_FIVE_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		ONE_HOUR + THIRTY_MINUTES, //
		TWO_HOUR, //
		THIRTY_MINUTES //
	};
}
