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
    private val _status = MutableLiveData<String>()

    // The external immutable LiveData for the request status String
    val status: LiveData<String>
        get() = _status

    // A Mars property
    private var _properties = MutableLiveData<List<MarsProperty>>()
    val properties: LiveData<List<MarsProperty>>
        get() = _properties

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
                        if (propertyResponse.body() != null ) {
                            _properties.value = propertyResponse.body()
                        } else {
                            _status.value = "No Available Properties"
                        }
                    } else {
                        _status.value = "Service returned ${propertyResponse.code()}"
                    }
                } catch (e: Exception) {
                    _status.value = "Error ${e.message}"
                } catch (e: Throwable) {
                    _status.value = "Something went wrong"
                }
            }
        }
    }

//    /**
//     * Following example from https://blog.mindorks.com/using-retrofit-with-kotlin-coroutines-in-android
//     */
//    fun getMarsMindorks() = liveData(Dispatchers.IO) {
//        emit(Resource.loading(data = null))
//        try {
//            emit (Resource.success(data = MarsApi.retrofitService.getProperties()))
//        } catch (exception: Exception) {
//            emit (Resource.error(data = null, message = exception.localizedMessage ?: "Error has occured"))
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //    /**
//     * Sets the value of the status LiveData to the Mars API status.
//     */
//    fun getMarsRealEstateProperties() = liveData(Dispatchers.IO) {
//        emit(Resource.loading(data = null))
//        try {
//            emit(Resource.success(MarsApi.retrofitService.getProperties()))
//        } catch (exception: Exception) {
//            emit(Resource.error(data = null, message = exception.message ?: "Unknown Error Occurred"))
//        }
//    }

//  Retrofit Callback interface style
//     {
//        MarsApi.retrofitService.getPropertes().enqueue(object : Callback<List<MarsProperty>>{
//            override fun onFailure(call: Call<List<MarsProperty>>, t: Throwable) {
//                _response.value = "Failure: ${t.message}"
//            }
//
//            override fun onResponse(call: Call<List<MarsProperty>>, response: Response<List<MarsProperty>>) {
//                val propertyCount = response.body()?.size ?: 0
//                _response.value = "Hurry!!! Only $propertyCount properties left"
//            }
//        })
//    }
}
