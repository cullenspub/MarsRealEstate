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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsProperty
import com.example.android.marsrealestate.utils.MarsApiStatus
import com.example.android.marsrealestate.utils.Status
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
class OverviewViewModel : ViewModel() {

    // Setup for using Coroutines
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob) + Dispatchers.IO

    // The internal MutableLiveData String that stores the status of the most recent request
    private var _status = MutableLiveData<MarsApiStatus<List<MarsProperty>>>()

    // The external immutable LiveData for the request status String
    val status: LiveData<MarsApiStatus<List<MarsProperty>>>
        get() = _status

//    // A Mars property
//    private var _properties = MutableLiveData<List<MarsProperty>>()
//    val properties: LiveData<List<MarsProperty>>
//        get() = _properties

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstatePropertiesV2()
    }

    /**
     * Using https://android.jlelse.eu/kotlin-coroutines-and-retrofit-e0702d0b8e8f as a guide for
     * invoking RetroFit >= 2.6 service
     */
    fun getMarsRealEstatePropertiesV2() {
        coroutineScope.launch {
            val propertyResponse = MarsApi.retrofitService.getProperties()
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
