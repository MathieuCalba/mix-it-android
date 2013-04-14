package fr.mixit.android.model;

public class Planning {

	public static final long TIMESTAMP_OFFSET_DAY_ONE = 1366840800000L; // 25 à 00h
	public static final long TIMESTAMP_OFFSET_DAY_TWO = 1366927200000L; // 26 à 00h

	public static final long THIRTY_MINUTES = 1800000L;
	public static final long ONE_HOUR = 3600000L;
	public static final long ONE_HOUR_AND_HALF = 5400000L;

	public static final long EIGHT_AM = 28800000L;
	public static final long NINE_AM = EIGHT_AM + ONE_HOUR;
	public static final long NINE_AND_A_HALF_AM = NINE_AM + THIRTY_MINUTES;
	public static final long TEN_AND_A_HALF_AM = NINE_AND_A_HALF_AM + ONE_HOUR;
	public static final long ELEVEN_AM = TEN_AND_A_HALF_AM + THIRTY_MINUTES;
	public static final long TWELVE_PM = ELEVEN_AM + ONE_HOUR;
	public static final long TWELVE_AND_A_HALF_PM = TWELVE_PM + THIRTY_MINUTES;
	public static final long ONE_PM = TWELVE_AND_A_HALF_PM + THIRTY_MINUTES;
	public static final long ONE_AND_A_HALF_PM = ONE_PM + THIRTY_MINUTES;
	public static final long TWO_AND_A_HALF_PM = ONE_AND_A_HALF_PM + ONE_HOUR;
	public static final long THREE_PM = TWO_AND_A_HALF_PM + THIRTY_MINUTES;
	public static final long FOUR_PM = THREE_PM + ONE_HOUR;
	public static final long FOUR_AND_A_HALF_PM = FOUR_PM + THIRTY_MINUTES;
	public static final long FIVE_AND_A_HALF_PM = FOUR_AND_A_HALF_PM + ONE_HOUR;
	public static final long SIX_PM = FIVE_AND_A_HALF_PM + THIRTY_MINUTES;
	public static final long SIX_AND_A_HALF_PM = SIX_PM + THIRTY_MINUTES;
	public static final long SEVEN_PM = SIX_AND_A_HALF_PM + THIRTY_MINUTES;
}
