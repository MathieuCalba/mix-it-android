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

package fr.mixit.android.io;

import java.io.IOException;

/**
 * General {@link java.io.IOException} that indicates a problem occured while parsing or applying an {@link org.json.JSONArray}.
 */
public class JsonHandlerException extends IOException {
	private static final long serialVersionUID = -384549840810346698L;

	public JsonHandlerException(String message) {
		super(message);
	}

	public JsonHandlerException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

	@Override
	public String toString() {
		if (getCause() != null) {
			return getLocalizedMessage() + ": " + getCause();
		} else {
			return getLocalizedMessage();
		}
	}
}