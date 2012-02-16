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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class UIUtils {

    public static boolean isIceCreamSandwich() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    public static boolean isHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }

    public static String getHtmlString(String bentoName, String msg) {
        StringBuilder html = new StringBuilder("<html><head><style>");
        //html.append("body { font-family:Verdana; }");
        html.append("td { border:0px min-width:10px; }");
        html.append("table { background-color:#00FFFFFF; padding:1px;}");
        html.append("</style></head>");
        html.append("<body><div><table><tr>");
        html.append("<td><b>").append(bentoName).append("</b></td>");
        html.append("</tr><tr>");
        html.append("<td>").append(msg).append("</td>");
        html.append("</tr></table></body></div>");
        html.append("</html>");
        return html.toString();
    }
}
