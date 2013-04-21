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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import fr.mixit.android.MixItApplication;


public class NetworkUtils {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	private static final String TAG = NetworkUtils.class.getSimpleName();

	// Default timeout
	protected static final int SOCKET_TIMEOUT = 20000;
	protected static final int CONNECTION_TIMEOUT = 20000;
	protected static final int BUFFER_SIZE = 8192;

	// for constructing params
	protected static final char EQUAL = '=';
	protected static final char AND = '&';
	protected static final char INTERROGATION_POINT = '?';

	protected static Cookie cookieSession;
	protected static CookieManager cookieManager;

	public enum ConnectivityState {
		WIFI, CARRIER, NONE, UNKNOWN
	};

	public static class ResponseHttp {
		public String jsonText;
		public int status;
	}

	public static ConnectivityState getConnectivity(Context ctx) {
		ConnectivityState currentNetworkType;
		final ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected()) {
			NetworkInfo networkInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (networkInfo != null && networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
				currentNetworkType = ConnectivityState.WIFI;
			} else {
				networkInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (networkInfo != null && networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
					currentNetworkType = ConnectivityState.CARRIER;
				} else {
					// how can we be there ? no wifi and no mobile data but connected ?
					currentNetworkType = ConnectivityState.UNKNOWN;
				}
			}
		} else {
			currentNetworkType = ConnectivityState.NONE;
		}

		return currentNetworkType;
	}

	public static ResponseHttp sendURL(String url, boolean isPost, HashMap<String, String> args) {
		ResponseHttp response = null;
		// Check OS version
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) { // only for android older than 2.3 (API 9)
			response = getInputStreamFromDefaultHttpClient(url, isPost, buildParams(args));
		} else { // otherwise use HTTPUrlConnection
			response = getInputStreamFromHttpUrlConnection(url, isPost, buildParams(args));
		}

		return response;
	}

	static ResponseHttp getInputStreamFromDefaultHttpClient(String url, boolean isPost, String args) {
		final HttpParams httpParameters = new BasicHttpParams();

		// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(httpParameters, BUFFER_SIZE);

		final DefaultHttpClient httpClient = getDefaultHttpClient(httpParameters);

		final ResponseHttp myResponse = new ResponseHttp();

		synchronized (httpClient) {
			if (cookieSession != null) {
				httpClient.getCookieStore().clear();
				httpClient.getCookieStore().addCookie(cookieSession);
			}

			try {
				HttpResponse response = null;

				if (isPost) {
					final HttpPost httpPost = new HttpPost(url);
					if (args != null) {
						final StringEntity se = new StringEntity(args, HTTP.UTF_8);
						httpPost.setEntity(se);
					}
					response = httpClient.execute(httpPost);
				} else {
					String urlWithParams = url;
					if (!isPost && args != null) {
						urlWithParams = getGetURL(url, args);
					}
					final HttpGet httpGet = new HttpGet(urlWithParams);
					response = httpClient.execute(httpGet);
				}

				// Read data if entity is OK
				if (response != null) {
					myResponse.status = response.getStatusLine().getStatusCode();
					if (DEBUG_MODE) {
						Log.d(TAG, "Status code : " + myResponse.status);
					}
					if (response.getEntity() != null) {
						myResponse.jsonText = getJson(response.getEntity().getContent());
					}
				}

				cookieSession = getCookie(httpClient);

				// httpClient.getConnectionManager().shutdown();
			} catch (final UnsupportedEncodingException e) {
				if (DEBUG_MODE) {
					Log.e(TAG, "UTF8 unsupported", e);
				}
			} catch (final IllegalArgumentException e) {
				if (DEBUG_MODE) {
					Log.e(TAG, "Invalid URL", e);
				}
			} catch (final ClientProtocolException e) {
				if (DEBUG_MODE) {
					Log.e(TAG, "Impossible to get data", e);
				}
			} catch (final IllegalStateException e) {
				if (DEBUG_MODE) {
					Log.e(TAG, "Impossible to read the data", e);
				}
			} catch (final IOException e) {
				if (DEBUG_MODE) {
					Log.e(TAG, "Impossible to get or read the data", e);
				}
			}
		}

		return myResponse;
	}

	static DefaultHttpClient myHttpClient;

	private static DefaultHttpClient getDefaultHttpClient(HttpParams httpParameters) {
		if (myHttpClient == null) {
			myHttpClient = new DefaultHttpClient(httpParameters);
		}
		return myHttpClient;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	static ResponseHttp getInputStreamFromHttpUrlConnection(String url, boolean isPost, String args) {
		final ResponseHttp myResponse = new ResponseHttp();

		HttpURLConnection urlConnection = null;

		if (cookieManager == null) {
			cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
		}

		try {
			String urlWithParams = url;
			if (!isPost && args != null) {
				urlWithParams = getGetURL(url, args);
			}
			final URL urlObject = new URL(urlWithParams);
			urlConnection = (HttpURLConnection) urlObject.openConnection();
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			urlConnection.setReadTimeout(SOCKET_TIMEOUT);

			// if (cookieSession != null) {
			// cookieManager.getCookieStore().removeAll();
			// cookieManager.getCookieStore().addCookie(cookieSession);
			// }

			if (isPost) {
				urlConnection.setDoOutput(true);
			}

			// urlConnection.connect();
			if (isPost && args != null) {
				final byte[] params = args.getBytes(HTTP.UTF_8);
				urlConnection.setFixedLengthStreamingMode(params.length);// or urlConnection.setChunkedStreamingMode(0);

				final OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
				out.write(params);
			}
			myResponse.status = urlConnection.getResponseCode();
			if (DEBUG_MODE) {
				Log.d(TAG, "Status code : " + myResponse.status);
			}
			myResponse.jsonText = getJson(urlConnection.getInputStream());
		} catch (final MalformedURLException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "The URL is malformatted", e);
			}
		} catch (final IllegalAccessError e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "setDoOutput after openning a connection or already done");
			}
		} catch (final UnsupportedEncodingException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "UTF8 unsupported for args", e);
			}
		} catch (final IllegalStateException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "I/O Error", e);
			}
		} catch (final IllegalArgumentException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "I/O Error", e);
			}
		} catch (final IOException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "I/O Error", e);
			}
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return myResponse;
	}

	static String buildParams(HashMap<String, String> args) {
		if (args != null && !args.isEmpty()) {
			final StringBuilder params = new StringBuilder();
			final Set<String> keys = args.keySet();
			for (final Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
				final String key = iterator.next();
				final String value = args.get(key);

				params.append(key);
				params.append(EQUAL);
				params.append(value);
				if (iterator.hasNext()) {
					params.append(AND);
				}
			}

			return params.toString();
		}
		return null;
	}

	static String getGetURL(String url, String params) {
		if (params != null) {
			final StringBuilder urlWithParams = new StringBuilder(url);
			urlWithParams.append(INTERROGATION_POINT);
			urlWithParams.append(params);
			return urlWithParams.toString();
		}
		return url;
	}

	static Cookie getCookie(DefaultHttpClient httpClient) {
		Cookie c = null;
		for (final Cookie cookie : httpClient.getCookieStore().getCookies()) {
			if (cookie != null) {
				c = cookie;
				if (DEBUG_MODE) {
					Log.i("AppInfosFragment", "cookieInfos : "//
							+ "comment:" + cookie.getComment() + //
							" commentURL:" + cookie.getCommentURL() + //
							" domain:" + cookie.getDomain() + //
							" name:" + cookie.getName() + //
							" path:" + cookie.getPath() + //
							" value:" + cookie.getValue() + //
							" version:" + cookie.getVersion() + //
							" expiryDate:" + cookie.getExpiryDate());
				}
			}
		}

		return c;
	}

	static String getJson(InputStream is) {
		String result = null;
		try {
			if (is != null) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8096);
				final StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				result = sb.toString();

				if (DEBUG_MODE) {
					Log.d(TAG, "InputStream : " + result);
				}
			} else {
				if (DEBUG_MODE) {
					Log.w(TAG, "Error while converting InputStream to String for JSON parsing : InputStream is null");
				}
			}
		} catch (final IOException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "Error while converting InputStream to String for JSON parsing", e);
			}
			result = null;
		}
		return result;
	}

}
