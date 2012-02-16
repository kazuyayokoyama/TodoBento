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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.os.Environment;

public class JpgFileHelper {
	public static final String extStorageDirectory = Environment
			.getExternalStorageDirectory().toString() + "/TodoBentoPics/";
	public static final String tmpImageFile = extStorageDirectory + "tmp.jpg";

	public static void saveJpegFile(String fileName, Bitmap bitmap) {
		// file
		File file = new File(extStorageDirectory, fileName + ".jpg");
		File fileDirectory = new File(extStorageDirectory);
		if (!fileDirectory.exists()) {
			fileDirectory.mkdir();
		}
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean deleteJpegFile(String fileName) {
		boolean ret = false;
		File file = new File(extStorageDirectory, fileName + ".jpg");
		if (!file.exists()) {
		} else {
			ret = file.delete();
		}
		return ret;
	}
	
	public static File getTmpFile() {
		File fileDirectory = new File(extStorageDirectory);
		if (!fileDirectory.exists()) {
			fileDirectory.mkdir();
		}
		return new File(tmpImageFile);
	}
}
