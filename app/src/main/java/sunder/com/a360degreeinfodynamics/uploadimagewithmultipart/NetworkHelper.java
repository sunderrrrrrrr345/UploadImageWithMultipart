package sunder.com.a360degreeinfodynamics.uploadimagewithmultipart;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.support.v4.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;


public class NetworkHelper {
	private static NetworkHelper instance;
	private Context context;
	private ImageLoader mImageLoader;
	private RequestQueue mRequestQueue;
	private String userAgent;
	public static final String TAG = NetworkHelper.class
            .getSimpleName();

	private NetworkHelper(Context context) {
		this.context = context;
		// Instantiate the cache
		Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

		Network network = new BasicNetwork(new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));

		mRequestQueue = new RequestQueue(cache, network);
		mRequestQueue.start();

		mImageLoader = new ImageLoader(mRequestQueue,
				new ImageLoader.ImageCache() {
					private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(
							20);

					@Override
					public Bitmap getBitmap(String url) {
						return cache.get(url);
					}

					@Override
					public void putBitmap(String url, Bitmap bitmap) {
						cache.put(url, bitmap);
					}
				});

	}

	/**
	 * @param context
	 * @return new instance of network helper.
	 */
	public static NetworkHelper getInstance(Context context) {
		instance = instance == null ? new NetworkHelper(context) : instance;
		return instance;
	}

	/**
	 * @return request queue for add and and execute request.
	 */
	public RequestQueue getRequestQueue() {

		return mRequestQueue;
	}

	/**
	 * @return The path of the directory holding application cache files.
	 */
	private File getCacheDir() {
		return context.getCacheDir();
	}


	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}


}
