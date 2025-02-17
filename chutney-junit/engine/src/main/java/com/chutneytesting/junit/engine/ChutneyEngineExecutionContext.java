/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.junit.engine;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.glacio.api.ExecutionRequestMapper;
import io.reactivex.rxjava3.core.Observable;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

public class ChutneyEngineExecutionContext implements EngineExecutionContext {

    private final ExecutionConfiguration executionConfiguration;
    private final String environmentName;

    protected ChutneyEngineExecutionContext(ExecutionConfiguration executionConfiguration, String environmentName) {
        this.executionConfiguration = executionConfiguration;
        this.environmentName = environmentName;
    }

    protected Observable<StepExecutionReportDto> executeScenario(StepDefinitionDto stepDefinitionDto) {
        Long executionId = executionConfiguration.embeddedTestEngine().executeAsync(ExecutionRequestMapper.toDto(stepDefinitionDto, environmentName));
        return executionConfiguration.embeddedTestEngine().receiveNotification(executionId);
    }

}
