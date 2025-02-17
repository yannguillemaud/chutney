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

import { Component, HostBinding, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '@model';
import { LoginService } from '@core/services';
import { ThemeService } from '@core/theme/theme.service';
import { LayoutOptions } from '@core/layout/layout-options.service';

@Component({
    selector: 'chutney-chutney-main-header',
    templateUrl: './chutney-main-header.component.html',
    styleUrls: ['./chutney-main-header.component.scss']
})
export class ChutneyMainHeaderComponent implements OnInit {

    public user$: Observable<User>;

    constructor(private loginService: LoginService,
                private themeService: ThemeService,
                public globals: LayoutOptions) {
        this.user$ = this.loginService.getUser();
    }

    @HostBinding('class.isActive')
    get isActiveAsGetter() {
        return this.isActive;
    }

    isActive: boolean;


    toggleSidebarMobile() {
        this.globals.toggleSidebarMobile = !this.globals.toggleSidebarMobile;
    }

    ngOnInit(): void {
    }

    logout() {
        this.loginService.logout();
    }

    public switchTheme() {
        this.themeService.switchTheme();
    }

    isLight(): boolean {
        return this.themeService.isLight()
    }

    isDark(): boolean {
        return !this.isLight()
    }
}
