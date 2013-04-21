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

import android.content.Context;
import android.database.Cursor;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public abstract class PlanningSlot {

	public static final int TYPE_SESSION = 1304141533;
	public static final int TYPE_NO_SESSION = 1304141534;
	public static final int TYPE_LIGHTNING_TALK = 1304141535;
	public static final int TYPE_KEYNOTE = 1304141536;
	public static final int TYPE_BREAK = 1304141537;
	public static final int TYPE_LUNCH = 1304141538;
	public static final int TYPE_BREAKFAST = 1304141539;
	public static final int TYPE_WELCOME = 1304141540;
	public static final int TYPE_TALKS_PRESENTATION = 1304141541;

	protected final String mSessionId;
	protected String mTitle;
	protected long mStart;
	protected long mEnd;
	protected String mRoomId;
	protected final String mFormat;
	protected final String mLevel;
	protected final String mLang;
	protected int mNbConcurrent = 0;
	protected final int mSlotType;

	protected PlanningSlot(Cursor c) {
		super();

		mSessionId = c.getString(MixItContract.Sessions.PROJ_LIST.SESSION_ID);
		mTitle = c.getString(MixItContract.Sessions.PROJ_LIST.TITLE);
		mStart = c.getLong(MixItContract.Sessions.PROJ_LIST.START);
		mEnd = c.getLong(MixItContract.Sessions.PROJ_LIST.END);
		mRoomId = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
		mFormat = c.getString(MixItContract.Sessions.PROJ_LIST.FORMAT);
		mLevel = c.getString(MixItContract.Sessions.PROJ_LIST.LEVEL);
		mLang = c.getString(MixItContract.Sessions.PROJ_LIST.LANG);
		mNbConcurrent = 1;
		mSlotType = TYPE_SESSION;
	}

	protected PlanningSlot(int slotType, long start, long end, String title, String room) {
		super();

		mSessionId = "-1";
		mTitle = title;
		mStart = start;
		mEnd = end;
		mRoomId = room;
		mFormat = null;
		mLevel = null;
		mLang = null;
		mNbConcurrent = 0;
		mSlotType = slotType;
	}

	public void addSession(Context ctx, PlanningSlot planningSlot, long minSlotStart, long maxSlotEnd) {
		final long start = planningSlot.getStart();
		final long end = planningSlot.getEnd();

		if (start < mStart) {
			// if (start < minSlotStart) {
			// mStart = minSlotStart;
			// } else {
			mStart = start;
			// }
		}
		if (end > mEnd) {
			// if (end > maxSlotEnd) {
			// mEnd = maxSlotEnd;
			// } else {
			mEnd = end;
			// }
		}

		mTitle = ctx.getResources().getString(R.string.planning_multiple_session);
		mRoomId = ctx.getResources().getString(R.string.planning_every_room);

		mNbConcurrent++;
	}

	public String getSessionId() {
		return mSessionId;
	}

	public String getTitle() {
		return mTitle;
	}

	public long getStart() {
		return mStart;
	}

	public long getEnd() {
		return mEnd;
	}

	public String getRoomId() {
		return mRoomId;
	}

	public String getFormat() {
		return mFormat;
	}

	public String getLevel() {
		return mLevel;
	}

	public String getLang() {
		return mLang;
	}

	public int getNbConcurrent() {
		return mNbConcurrent;
	}

	public int getSlotType() {
		return mSlotType;
	}

	@Override
	public String toString() {
		return "PlanningSlot [mSessionId=" + mSessionId + ", mTitle=" + mTitle + ", mStart=" + mStart + ", mEnd=" + mEnd + ", mRoomId=" + mRoomId
				+ ", mFormat=" + mFormat + ", mLevel=" + mLevel + ", mLang=" + mLang + ", mNbConcurrent=" + mNbConcurrent + "]";
	}

	public static class NoSessionSlot extends PlanningSlot {

		public NoSessionSlot(Context ctx, long start, long end) {
			super(TYPE_NO_SESSION, start, end, ctx.getResources().getString(R.string.planning_no_session), null);
		}

	}

	public static class SessionSlot extends PlanningSlot {

		public SessionSlot(Cursor c) {
			super(c);
		}

	}

	public static class BreakfastSlot extends PlanningSlot {

		public BreakfastSlot(Context ctx, long start, long end) {
			super(TYPE_BREAKFAST, start, end, ctx.getResources().getString(R.string.planning_breakfast), null);
		}

	}

	public static class WelcomeSlot extends PlanningSlot {

		public WelcomeSlot(Context ctx, long start, long end) {
			super(TYPE_WELCOME, start, end, ctx.getResources().getString(R.string.planning_welcome), null);
		}

	}

	public static class TalksPresentationSlot extends PlanningSlot {

		public TalksPresentationSlot(Context ctx, long start, long end) {
			super(TYPE_TALKS_PRESENTATION, start, end, ctx.getResources().getString(R.string.planning_talks_presentation), ctx.getResources().getString(
					R.string.planning_no_room));
		}

	}

	public static class LunchSlot extends PlanningSlot {

		public LunchSlot(Context ctx, long start, long end) {
			super(TYPE_LUNCH, start, end, ctx.getResources().getString(R.string.planning_lunch), null);
		}

	}

	public static class BreakSlot extends PlanningSlot {

		public BreakSlot(Context ctx, long start, long end) {
			super(TYPE_BREAK, start, end, ctx.getResources().getString(R.string.planning_break), null);
		}

	}

	public static class LightningTalkSlot extends PlanningSlot {

		public LightningTalkSlot(Context ctx, long start, long end) {
			super(TYPE_LIGHTNING_TALK, start, end, ctx.getResources().getString(R.string.planning_lightning_talk), ctx.getResources().getString(
					R.string.planning_lightning_room));
		}

	}

}
