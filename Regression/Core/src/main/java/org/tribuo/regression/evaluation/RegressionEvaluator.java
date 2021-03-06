/*
 * Copyright (c) 2015-2020, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tribuo.regression.evaluation;

import org.tribuo.Model;
import org.tribuo.Prediction;
import org.tribuo.evaluation.AbstractEvaluator;
import org.tribuo.evaluation.Evaluator;
import org.tribuo.evaluation.metrics.EvaluationMetric;
import org.tribuo.evaluation.metrics.MetricID;
import org.tribuo.evaluation.metrics.MetricTarget;
import org.tribuo.provenance.EvaluationProvenance;
import org.tribuo.regression.Regressor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link Evaluator} for multi-dimensional regression using {@link Regressor}s.
 */
public final class RegressionEvaluator extends AbstractEvaluator<Regressor, RegressionMetric.Context, RegressionEvaluation, RegressionMetric> {

    private final boolean useExampleWeights;

    /**
     * By default, don't use example weights.
     */
    public RegressionEvaluator() {
        this(false);
    }

    /**
     * Construct an evaluator.
     * <p>
     * Will weight the examples if requested.
     * @param useExampleWeights Set to true to use the example weights to adjust the importance of the predictions.
     */
    public RegressionEvaluator(boolean useExampleWeights) {
        this.useExampleWeights = useExampleWeights;
    }

    @Override
    protected Set<RegressionMetric> createMetrics(Model<Regressor> model) {
        Set<RegressionMetric> metrics = new HashSet<>();
        for (Regressor variable : model.getOutputIDInfo().getDomain()) {
            MetricTarget<Regressor> target = new MetricTarget<>(variable);
            metrics.add(RegressionMetrics.R2.forTarget(target));
            metrics.add(RegressionMetrics.RMSE.forTarget(target));
            metrics.add(RegressionMetrics.MAE.forTarget(target));
            metrics.add(RegressionMetrics.EV.forTarget(target));
        }
        MetricTarget<Regressor> macroAverage = new MetricTarget<>(EvaluationMetric.Average.MACRO);
        metrics.add(RegressionMetrics.R2.forTarget(macroAverage));
        metrics.add(RegressionMetrics.RMSE.forTarget(macroAverage));
        metrics.add(RegressionMetrics.MAE.forTarget(macroAverage));
        metrics.add(RegressionMetrics.EV.forTarget(macroAverage));
        return metrics;
    }

    @Override
    protected RegressionMetric.Context createContext(Model<Regressor> model, List<Prediction<Regressor>> predictions) {
        return new RegressionMetric.Context(model, predictions, useExampleWeights);
    }

    @Override
    protected RegressionEvaluation createEvaluation(RegressionMetric.Context context,
                                                    Map<MetricID<Regressor>, Double> results,
                                                    EvaluationProvenance provenance) {
        return new RegressionEvaluationImpl(results, context, provenance);
    }
}