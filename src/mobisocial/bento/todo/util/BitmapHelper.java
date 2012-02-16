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

package mobisocial.bento.todo.util;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapHelper {
	public static final int MAX_IMAGE_WIDTH = 320;
	public static final int MAX_IMAGE_HEIGHT = 320;

	public static byte[] bitmapToBytes(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static Bitmap getResizedBitmap(File file, int targetWidth,
			int targetHeight, float degrees) {
		Bitmap ret = null;

		if (file == null) {
		} else {
			BitmapFactory.Options option = new BitmapFactory.Options();
			Bitmap src = null;
			int sampleSize = 0;

			option.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getAbsolutePath(), option);
			// more than 1MP
			if ((option.outWidth * option.outHeight) > 1048576) {
				double out_area = (double) (option.outWidth * option.outHeight) / 1048576.0;
				sampleSize = (int) (Math.sqrt(out_area) + 1);
			} else {
				sampleSize = 1;
			}

			option.inJustDecodeBounds = false;
			option.inSampleSize = sampleSize;
			src = BitmapFactory.decodeFile(file.getAbsolutePath(), option);
			if (src == null) {
			} else {
				ret = getResizedBitmap(src, targetWidth, targetHeight, degrees);
			}
		}

		return ret;
	}

	public static Bitmap getResizedBitmap(Bitmap src, int targetWidth,
			int targetHeight, float degrees) {
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();

		float scale = getFitScale(targetWidth, targetHeight, srcWidth, srcHeight);

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		matrix.postRotate(degrees);

		return Bitmap.createBitmap(src, 0, 0, srcWidth, srcHeight, matrix, true);
	}

	public static float getFitScale(int targetWidth, int targetHeight,
			int srcWidth, int srcHeight) {
		float ret = 0;

		if (targetWidth < targetHeight) {
			if (srcWidth < srcHeight) {
				ret = (float) targetHeight / (float) srcHeight;
				if ((srcWidth * ret) > targetWidth) {
					ret = (float) targetWidth / (float) srcWidth;
				}
			} else {
				ret = (float) targetWidth / (float) srcWidth;
			}
		} else {
			if (srcWidth < srcHeight) {
				ret = (float) targetHeight / (float) srcHeight;
			} else {
				ret = (float) targetWidth / (float) srcWidth;
				if ((srcHeight * ret) > targetHeight) {
					ret = (float) targetHeight / (float) srcHeight;
				}
			}
		}

		return ret;
	}

	public static Bitmap getDummyBitmap(int targetWidth, int targetHeight) {
		Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.LTGRAY);
		canvas.drawPaint(paint);
		return bitmap;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// paint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap getScreenFitBitmap(Bitmap src, int dispWidth, int dispHeight) {
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();

		float scale = (float) dispWidth / srcWidth;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		return Bitmap.createBitmap(src, 0, 0, srcWidth, srcHeight, matrix, true);
	}
}
