package org.rapidoid.webapp;

/*
 * #%L
 * rapidoid-http
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
 * %%
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
 * #L%
 */

import org.junit.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.http.HttpTestCommons;
import org.rapidoid.io.Res;
import org.rapidoid.jackson.YAML;

@Authors("Nikolche Mihajlovski")
@Since("4.1.0")
public class AppMenuTest extends HttpTestCommons {

	@Test
	public void shouldConstructAppMenu() {
		Res res = Res.from("menu.yaml");

		Object data = YAML.parse(res.getContent(), Object.class);
		System.out.println(data);

		AppMenu menu = AppMenu.from(data);
		System.out.println(menu);

		eq(menu.getItems().size(), 2);

		AppMenuItem item1 = menu.getItems().get(0);
		eq(item1.getCaption(), "item1");
		eq(item1.getTarget(), "aaa");

		AppMenuItem item2 = menu.getItems().get(1);
		eq(item2.getCaption(), "item2");
		isNull(item2.getTarget());

		AppSubMenuItem subitem1 = item2.getSubmenu().getItems().get(0);
		AppSubMenuItem subitem2 = item2.getSubmenu().getItems().get(1);
		AppSubMenuItem subitem3 = item2.getSubmenu().getItems().get(2);

		eq(subitem1.getCaption(), "d");
		eq(subitem1.getTarget(), "");

		eq(subitem2.getCaption(), "e");
		eq(subitem2.getTarget(), "f");

		eq(subitem3.getCaption(), "last");
		eq(subitem3.getTarget(), "");
	}

}