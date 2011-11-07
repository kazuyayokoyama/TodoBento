/*
 * Copyright (C) 2011 Kazuya Yokoyama <kazuya.yokoyama@gmail.com>
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

package com.kazuyayokoyama.android.todobento.ui.list;

public class TodoListItem {
	public String uuid;				// UUID
	public Boolean bDone;			// CheckBox (Done/Unfinished)
	public String title;			// ToDo title
	public String description;		// ToDo description
	public boolean withImg;			// With Image
	public long creDateMillis;		// Creation Date (Millisecond)
	public long modDateMillis;		// Modification Date (Millisecond)
	public String creContactId;		// Contact ID who created
	public String modContactId;		// Contact ID who modified
}
