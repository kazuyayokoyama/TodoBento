/*
 * Copyright (C) 2012 Kazuya (Kaz) Yokoyama <kazuya.yokoyama@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kazuyayokoyama.android.apps.todobento.util;

import java.util.HashMap;

import android.graphics.Bitmap;

public class ImageCache {

	private static HashMap<String, Bitmap> sCache = new HashMap<String, Bitmap>();

	public static Bitmap getImage(String key) {
		if (sCache.containsKey(key)) {
			return sCache.get(key);
		}
		return null;
	}

	public static void setImage(String key, Bitmap image) {
		sCache.put(key, image);
	}

	public static void clearCache() {
		sCache = null;
		sCache = new HashMap<String, Bitmap>();
	}
}
