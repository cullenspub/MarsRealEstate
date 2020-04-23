/*
 * Copyright 2018, The Android Open Source Project
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
 *
 */

package com.example.android.marsrealestate.overview

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.R
import com.example.android.marsrealestate.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.launch


/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 *
 * Code for Retrofit and Coroutines adapted from https://blog.mindorks.com/using-retrofit-with-kotlin-coroutines-in-android
 *
 */
class OverviewViewModel(private val application: Application) : ViewModel() {

    // Setup for using Coroutines
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob) + Dispatchers.IO

    // The internal MutableLiveData String that stores the status of the most recent request
    private var _status = MutableLiveData<MarsApiStatus<List<MarsProperty>>>()

    // The external immutable LiveData for the request status String
    val status: LiveData<MarsApiStatus<List<MarsProperty>>>
        get() = _status

    // LiveData to trigger navigation to detail view
    private var _navigateToSelectedProperty = MutableLiveData<MarsProperty>()
    val navigateToSelectedProperty: LiveData<MarsProperty>
        get() = _navigateToSelectedProperty


    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstatePropertiesV2(MarsApiFilter.SHOW_ALL)
    }

    // Function to set the selected LiveData property
    fun displayPropertyDetails(marsProperty: MarsProperty) {
        _navigateToSelectedProperty.value = marsProperty
    }

    // Function to reset the Mars property after navigation is complete
    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

    /**
     * Using https://android.jlelse.eu/kotlin-coroutines-and-retrofit-e0702d0b8e8f as a guide for
     * invoking RetroFit >= 2.6 service
     */
    fun getMarsRealEstatePropertiesV2(filter: MarsApiFilter) {
        coroutineScope.launch {
            val propertyResponse = MarsApi.retrofitService.getProperties(filter.value)
            withContext(Dispatchers.Main) {
                try {
                    if (propertyResponse.isSuccessful) {
                        _status.value = MarsApiStatus(Status.SUCCESS, propertyResponse.body(), null)
                    } else {
                        _status.value = MarsApiStatus(Status.ERROR, null, "Response ${propertyResponse.code()}")
                    }
                } catch (e: Exception) {
                    _status.value = MarsApiStatus(Status.ERROR, null, e.message)
                } catch (e: Throwable) {
                    _status.value = MarsApiStatus(Status.ERROR, null, e.message)
                }
            }
        }
    }

    fun updateFilter(filter: MarsApiFilter) {
        getMarsRealEstatePropertiesV2(filter)
    }


    fun displayablePropertyType(property: MarsProperty): String {
            return application.applicationContext.getString(R.string.display_type,
                    application.applicationContext.getString(
                            when(property.isRental) {
                                true -> R.string.type_rent
                                false -> R.string.type_sale
                            }))
        }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
