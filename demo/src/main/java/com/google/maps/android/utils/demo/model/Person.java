/*
 * Copyright 2023 Google Inc.
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

package com.google.maps.android.utils.demo.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;


public class Person implements ClusterItem {
    public final String name;
    public final int profilePhoto;
    private final LatLng mPosition;

    public Person(LatLng position, String name, int pictureResource) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    // If we use the diff() operation, we need to implement an equals operation, to determine what
    // makes each ClusterItem unique (which is probably not the position)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj != null && getClass() != obj.getClass()) return false;
        Person myObj = (Person) obj;
        return this.name.equals(myObj.name);
    }
}
