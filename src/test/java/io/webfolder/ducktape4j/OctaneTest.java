/*
 * Copyright (C) 2015 Square, Inc.
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
package io.webfolder.ducktape4j;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import okio.BufferedSource;
import okio.Okio;

public class OctaneTest {

    @Test
    @Ignore
    public void test() throws Throwable {
        Duktape duktape = Duktape.create();
        String[] files = new File("src/test/resources/octane").list();
        Arrays.sort(files);
        for (String file : files) {
            long tookMs = evaluateAsset(duktape, "octane/" + file);
            StringBuilder output = new StringBuilder();
            output.append(file).append(" eval took ").append(tookMs).append(" ms");
            System.out.println(output.toString());
            evaluateAsset(duktape, "octane.js");
            String results = (String) duktape.evaluate("getResults();");
            System.out.println(results);
        }
    }

    private long evaluateAsset(Duktape duktape, String file) throws Throwable {
        BufferedSource source = Okio.buffer(Okio.source(new File("src/test/resources/" + file)));
        String script = source.readUtf8();
        source.close();

        long startNanos = System.nanoTime();
        try {
            duktape.evaluate(script, file);
        } catch (Throwable t) {
            System.err.println(file);
            throw t;
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
