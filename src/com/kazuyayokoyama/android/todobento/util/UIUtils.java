/*
 * Copyright 2011 Google Inc.
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
 * 
 * ChangeLog 
 *   2011-11-07 Kazuya Yokoyama <kazuya.yokoyama@gmail.com>
 *   - Modified package name
 *   - Added some functions
 *   
 */

package com.kazuyayokoyama.android.todobento.util;

import java.util.List;
import java.util.TimeZone;

import com.kazuyayokoyama.android.todobento.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {
    /**
     * Time zone to use when formatting all session times. To always use the
     * phone local time, use {@link TimeZone#getDefault()}.
     */

    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }

    public static long getCurrentTime(final Context context) {
        //SharedPreferences prefs = context.getSharedPreferences("mock_data", 0);
        //prefs.edit().commit();
        //return prefs.getLong("mock_current_time", System.currentTimeMillis());
        return System.currentTimeMillis();
    }

    public static Drawable getIconForIntent(final Context context, Intent i) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
        if (infos.size() > 0) {
            return infos.get(0).loadIcon(pm);
        }
        return null;
    }

    public static String getUpdateString(final Context context, String msg) {
        StringBuilder html = new StringBuilder("<html><head><style>");
        //html.append("body { font-family:Verdana; }");
        html.append("td { border:0px min-width:10px; }");
        html.append("table { background-color:#00FFFFFF; padding:1px;}");
        html.append("</style></head>");
        html.append("<body><div><table><tr>");
        html.append("<td><b>").append(context.getString(R.string.app_name)).append("</b></td>");
        html.append("</tr><tr>");
        html.append("<td>").append(msg).append("</td>");
        html.append("</tr></table></body></div>");
        html.append("</html>");
        return html.toString();
    }
}
