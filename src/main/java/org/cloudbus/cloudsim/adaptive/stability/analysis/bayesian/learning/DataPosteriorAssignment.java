/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.learning;

import org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.variables.Assignment;
import org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.variables.Variable;

/**
 * This class stores the posterior probabilities over a set of latent variables and
 * a joint assignment for another ones.
 */
public class DataPosteriorAssignment {

    DataPosterior posterior;
    Assignment assignment;

    public DataPosteriorAssignment(DataPosterior posterior, Assignment assignment) {
        this.posterior = posterior;
        this.assignment = assignment;
    }

    public DataPosterior getPosterior() {
        return posterior;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public boolean isObserved(Variable var){
        return !this.posterior.isPresent(var);
    }

    public String toString(){
        return this.posterior.toString() + "|" + assignment.outputString();
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
}
