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

package io.rollout.configuration.comparison;

import io.rollout.publicapi.model.Flag;
import io.rollout.publicapi.model.TargetGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that will compare two different Lists of objects. It will return a {@link ComparisonResult} that will supplied configurations into the following groups:
 * <ul>
 *     <li>Model elements that are present in the first list only</li>
 *     <li>Model elements that are present in the second list only</li>
 *     <li>Model elements that are present in the both list and the same in both Configurations</li>
 *     <li>Model elements that are present in the both list but the Configurations differ (but with the same ID)</li>
 * </ul>
 * When comparing the {@link Flag} and {@link TargetGroup} models, we use the ID field to determine whether it has changed between one object and
 * the other
 */
public class ConfigurationComparator {
    public <T> ComparisonResult<T> compare(List<T> first, List<T> second) {
        ComparisonResult<T> result = new ComparisonResult<>();

        // Work through the experiments first.
        // Create maps of the old config by ID and the new config by ID, and a set of IDs.
        Map<String, T> firstById = first.stream().collect(Collectors.toMap(this::getId, Function.identity()));
        Map<String, T> secondById = second.stream().collect(Collectors.toMap(this::getId, Function.identity()));
        Set<String> ids = new HashSet<>(firstById.keySet()); // make it mutable
        ids.addAll(secondById.keySet());

        ids.forEach(id -> {
            T firstModel = firstById.get(id);
            T secondModel = secondById.get(id);
            if (firstModel != null && secondModel != null) {
                if (firstModel.equals(secondModel)) {
                    result.addInBothAndTheSame(firstModel);
                } else {
                    result.addInBothButDifferent(firstModel, secondModel);
                }
            } else if (firstModel != null) {
                result.addInFirstOnly(firstModel);
            } else if (secondModel != null) {
                result.addInSecondOnly(secondModel);
            } else {
                throw new RuntimeException("Logic fail. We didn't handle experiment " + id);
            }
        });

        return result;
    }

    private String getId(Object model) {
        if (model instanceof Flag) {
            return ((Flag)model).getName();
        } else if (model instanceof TargetGroup) {
            return ((TargetGroup)model).getName();
        } else {
            throw new RuntimeException("Cannot do getId() on " + model.getClass().getSimpleName());
        }
    }

}
