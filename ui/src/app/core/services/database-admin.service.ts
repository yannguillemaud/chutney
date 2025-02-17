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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class DatabaseAdminService {

  private adminUrl = '/api/v1/admin/database';

  constructor(private http: HttpClient) { }

  execute(statement: string, database: string = 'jdbc'): Observable<Object> {
    return this.http.post(environment.backend + this.adminUrl + '/execute/' + database, statement);
  }

  paginate(statement: string, database: string = 'jdbc', pageNumber: number = 1, elementPerPage: number = 5): Observable<Object> {
    return this.http.post(environment.backend + this.adminUrl + '/paginate/' + database,
      {
        pageNumber: pageNumber,
        elementPerPage: elementPerPage,
        wrappedRequest: statement
      });
  }
}
