/**
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

import { Authorization } from '@model';
import { MenuItem } from '@shared/components/layout/menuItem';
import { FeatureName } from '@core/feature/feature.model';

export const allMenuItems: MenuItem [] = [
    {
        label: 'Navigation',
        children: [
            {
                label: 'menu.principal.scenarios',
                link: '/scenario',
                iconClass: 'fa fa-film',
                authorizations: [Authorization.SCENARIO_READ,Authorization.SCENARIO_WRITE,Authorization.SCENARIO_EXECUTE]
            },
            {
                label: 'menu.principal.campaigns',
                link: '/campaign',
                iconClass: 'fa fa-clock',
                authorizations: [Authorization.CAMPAIGN_READ, Authorization.CAMPAIGN_WRITE,Authorization.CAMPAIGN_EXECUTE]
            },
            {
                label: 'menu.principal.variable',
                link: '/variable',
                iconClass: 'fa fa-list-ul',
                authorizations: [Authorization.GLOBAL_VAR_READ,Authorization.GLOBAL_VAR_WRITE]
            },
            {
                label: 'menu.principal.dataset',
                link: '/dataset',
                iconClass: 'fa fa-table',
                authorizations: [Authorization.DATASET_READ,Authorization.DATASET_WRITE]
            },
        ],
    },
    {
        label: 'Admin',
        children: [
            {
                label: 'menu.principal.environments',
                link: '/environments',
                iconClass: 'fa fa-brands fa-envira',
                authorizations: [Authorization.ENVIRONMENT_ACCESS]
            },
            {
                label: 'menu.principal.targets',
                link: '/targets',
                iconClass: 'fa fa-bullseye',
                authorizations: [Authorization.ENVIRONMENT_ACCESS]
            },
            {
                label: 'menu.principal.plugins',
                link: '/plugins',
                iconClass: 'fa fa-cogs',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.roles',
                link: '/roles',
                iconClass: 'fa fa-user-shield',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.backups',
                link: '/backups',
                iconClass: 'fa fa-archive',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.databaseAdmin',
                link: '/databaseAdmin',
                iconClass: 'fa fa-database',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.workers',
                link: '/configurationAgent',
                iconClass: 'fa fa-bars',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.previewReport',
                link: '/scenario/report-preview',
                iconClass: 'fa fa-clipboard',
                authorizations: [Authorization.ADMIN_ACCESS]
            },
            {
                label: 'menu.principal.metrics',
                link: '/metrics',
                iconClass: 'fa fa-chart-simple',
                authorizations: [Authorization.ADMIN_ACCESS]
            }
        ]
    }

];
