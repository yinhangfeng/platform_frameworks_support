/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.arch.persistence.room.integration.testapp.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.util.paging.ListConfig;
import android.arch.util.paging.LiveLazyListProvider;

/**
 * Simple Customer DAO for Room Customer list sample.
 */
@Dao
public interface CustomerDao {

    /**
     * Insert a customer
     * @param customer Customer.
     */
    @Insert
    void insert(Customer customer);

    /**
     * Insert multiple customers.
     * @param customers Customers.
     */
    @Insert
    void insertAll(Customer[] customers);

    /**
     * @return LiveLazyListProvider of customers, ordered by last name. Call
     * {@link LiveLazyListProvider#create(ListConfig)} to get a LiveData of LazyLists.
     */
    @Query("SELECT * FROM customer ORDER BY mLastName ASC")
    LiveLazyListProvider<Customer> loadPagedAgeOrder();
}
