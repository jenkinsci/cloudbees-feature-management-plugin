/*
 * The MIT License
 *
 * Copyright 2015-2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.cloudbees.diff.Diff;
import io.rollout.publicapi.model.ConfigEntity;
import java.io.StringReader;
        return o == null ? "" : new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);

    public String generateDiff(ComparisonResult<? extends ConfigEntity> comparisonResult) {
        // Generate a Unified Diff with all the entity changes
        StringBuilder builder = new StringBuilder();

        // First get all the new elements
        comparisonResult.getInSecondOnly().forEach(entity -> {
            try {
                String json = toJson(entity);
                String diff = Diff.diff(new StringReader(""), new StringReader(json), true)
                        .toUnifiedDiff(entity.getName(), entity.getName(), new StringReader(""), new StringReader(json), 100); // Don't use Integer.MAX_VALUE here, but give it a big enough value so that it shows the whole config
                builder.append("diff\n")
                        .append("new file mode 100666\n")
                        .append(diff);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Now get all the deleted elements
        comparisonResult.getInFirstOnly().forEach(entity -> {
            try {
                String json = toJson(entity);
                String diff = Diff.diff(new StringReader(json), new StringReader(""), true)
                        .toUnifiedDiff(entity.getName(), entity.getName(), new StringReader(json), new StringReader(""), 100); // Don't use Integer.MAX_VALUE here, but give it a big enough value so that it shows the whole config
                builder.append("diff\n")
                        .append("deleted file mode 100666\n")
                        .append(diff);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Now get all the changed elements
        comparisonResult.getInBothButDifferent().stream()
            .forEach(entity -> {
                try {
                    String left = toJson(entity.getLeft()).trim();
                    String right = toJson(entity.getRight()).trim();
                    String name = entity.getLeft().getName();

                    String diff = Diff.diff(new StringReader(left), new StringReader(right), true)
                            .toUnifiedDiff(name, name, new StringReader(left), new StringReader(right), 100); // Don't use Integer.MAX_VALUE here, but give it a big enough value so that it shows the whole config
                    builder.append("diff\n").append(diff);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        return builder.toString();
    }
