/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.arch.persistence.room.solver

import android.arch.persistence.room.parser.ParsedQuery
import android.arch.persistence.room.processor.Context
import android.arch.persistence.room.processor.ProcessorErrors
import android.arch.persistence.room.solver.query.result.QueryResultAdapter
import android.arch.persistence.room.solver.query.result.QueryResultBinder
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Binder provider class that has common functionality for observables.
 */
abstract class ObservableQueryResultBinderProvider(val context: Context)
    : QueryResultBinderProvider {
    protected abstract fun extractTypeArg(declared: DeclaredType) : TypeMirror
    protected abstract fun create(typeArg: TypeMirror,
                                  resultAdapter: QueryResultAdapter?,
                                  tableNames : Set<String>) : QueryResultBinder

    override final fun provide(declared: DeclaredType, query: ParsedQuery): QueryResultBinder {
        val typeArg = extractTypeArg(declared)
        val adapter = context.typeAdapterStore.findQueryResultAdapter(typeArg, query)
        val tableNames = ((adapter?.accessedTableNames() ?: emptyList()) +
                query.tables.map { it.name }).toSet()
        context.checker.check(!tableNames.isEmpty(),
                declared.asElement(),
                ProcessorErrors.OBSERVABLE_QUERY_NOTHING_TO_OBSERVE)
        return create(
                typeArg = typeArg,
                resultAdapter = adapter,
                tableNames = tableNames
        )
    }
}