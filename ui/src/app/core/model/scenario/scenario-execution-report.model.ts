import { StepExecutionReport } from '@core/model/scenario/step-execution-report.model';

export class ScenarioExecutionReport {
    constructor(
        public executionId: string,
        public report: StepExecutionReport,
        public environment: string,
        public user: string,
        public scenarioName?: string
    ) { }
}
