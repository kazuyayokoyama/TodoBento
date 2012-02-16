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

package mobisocial.bento.todo.io;

import java.util.ArrayList;

import mobisocial.bento.todo.ui.TodoListItem;


public class Bento {
	public String uuid;
	public String name;
	public String creContactId;
	public int numberOfTodo;
	public ArrayList<TodoListItem> todoList;
	
	public Bento() {
		todoList = new ArrayList<TodoListItem>();
	}
}
