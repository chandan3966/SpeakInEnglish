/*
 * Copyright (C) 2017-2019 Hazuki
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

package com.example.speakinenglish.container;

import android.content.SharedPreferences;

public class StringContainer extends BaseContainer<String> {
    public StringContainer(String name, String def_value) {
        super(name, def_value);
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        mValue = shared_preference.getString(mName, mDefValue);
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        editor.putString(mName, mValue);
    }
}

