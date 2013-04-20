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