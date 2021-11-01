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
 */

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase(){
abstract val sleepDatabaseDao: SleepDatabaseDao

    //companion object allows clients to access methods for creating/getting the database
    //without instantiating the class. This class only provides the database, so it
    //doesn't need to be instantiated.
    //@Volatile annotation prevents the INSTANCE from being cached and keeps it up to date.
    //This means that all threads can see current value of INSTANCE
companion object{

        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase{
            //synchronized ensures only one thread can enter the block of code at a time and
            //ensures the database is only initialized once.
            synchronized(this){
                //Smart Cast ensures the return is always a SleepDatabase and can only be used
                //with local variables - not class variables.
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SleepDatabase::class.java,
                        "sleep_history_database"
                    )
                        //Migration object: codes how to convert a table from an old schema to a new one.
                        //Example: an app upgrade and change the # of rows/columns in the table while saving
                        //user data. fallbackToDestructiveMigration destroys the database in this situation
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }


}

