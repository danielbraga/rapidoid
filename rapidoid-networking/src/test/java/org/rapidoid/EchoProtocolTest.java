/*-
 * #%L
 * rapidoid-networking
 * %%
 * Copyright (C) 2014 - 2018 Nikolche Mihajlovski and contributors
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

package org.rapidoid;

import org.junit.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.buffer.BufUtil;
import org.rapidoid.commons.Rnd;
import org.rapidoid.docs.echoprotocol.EchoProtocol;
import org.rapidoid.io.IO;
import org.rapidoid.lambda.F3;
import org.rapidoid.net.util.NetUtil;
import org.rapidoid.u.U;
import org.rapidoid.util.Constants;
import org.rapidoid.util.Msc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Authors("Nikolche Mihajlovski")
@Since("2.0.0")
public class EchoProtocolTest extends NetTestCommons {

	private static final int ROUNDS = Msc.normalOrHeavy(1, 100);

	private static final int MAX_MSG_COUNT = Msc.normalOrHeavy(10, 1000);

	private static final List<String> testCases = U.list(
		"abc\nxy\nbye\n",
		"abc\r\nxy\r\nbye\r\n",
		"abc\nbye\n",
		"abc\r\nbye\r\n"
	);

	static {
		String s1 = "", s2 = "";

		for (int i = 0; i < MAX_MSG_COUNT; i++) {
			s1 += i + "\r\n";
			s2 += i + "\n";
		}

		testCases.add(s1 + "bye\r\n");
		testCases.add(s2 + "bye\n");
	}

	@Test
	public void echo() {
		server(new EchoProtocol(), this::connectAndExercise);
	}

	private void connectAndExercise() {

		NetUtil.connect("localhost", 8080, (F3<Void, InputStream, BufferedReader, DataOutputStream>) (inputStream, in, out) -> {
			out.writeBytes("hello\n");
			eq(in.readLine(), "HELLO");

			out.writeBytes("Foo\n");
			eq(in.readLine(), "FOO");

			out.writeBytes("bye\n");
			eq(in.readLine(), "BYE");

			return null;
		});

		for (int i = 1; i <= ROUNDS; i++) {
			Msc.logSection("ROUND " + i);

			for (final String testCase : testCases) {

				final List<String> expected = U.list(testCase.toUpperCase().split("\r?\n"));

				Msc.startMeasure();

				NetUtil.connect("localhost", 8080, (F3<Void, InputStream, BufferedReader, DataOutputStream>) (inputStream, in, out) -> {
					out.writeBytes(testCase);

					List<String> lines = IO.readLines(in);

					eq(lines, expected);
					return null;
				});

				Msc.endMeasure(expected.size(), "messages");
			}
		}
	}

	@Test
	public void echoAsync() {
		server(ctx -> {

			if (ctx.isInitial()) {
				BufUtil.doneWriting(ctx.output());
				return;
			}

			final String in = ctx.readln();
			final long handle = ctx.async();
			final long connId = ctx.connId();

			Msc.EXECUTOR.schedule(() -> ctx.resume(connId, handle, () -> {
				ctx.write(in.toUpperCase());
				ctx.write(Constants.CR_LF);
				ctx.send();

				ctx.closeIf(in.equals("bye"));
				return true; // finished
			}), Rnd.rnd(100), TimeUnit.MILLISECONDS);

		}, this::connectAndExercise);
	}

}
