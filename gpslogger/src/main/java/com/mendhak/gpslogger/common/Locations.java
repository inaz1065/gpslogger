/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.common;


import android.location.Location;
import android.support.annotation.NonNull;

public class Locations {


    public static Location getLocationWithAdjustedAltitude(Location loc, PreferenceHelper ph) {
        return getLocation(loc, ph);
    }

    @NonNull
    private static Location getLocation(Location loc, PreferenceHelper ph) {
        if(!loc.hasAltitude()){ return loc; }

        AdjustAltitudeFromGeoIdHeightAndGetExtraIsNotNull(loc, ph);

        LocationHasAltAndSubtractAltOffsetIsNotNull(loc, ph);

        return loc;
    }

    private static void AdjustAltitudeFromGeoIdHeightAndGetExtraIsNotNull(Location loc, PreferenceHelper ph) {
        if(ph.shouldAdjustAltitudeFromGeoIdHeight() && loc.getExtras() != null){
            String geoidheight = loc.getExtras().getString(BundleConstants.GEOIDHEIGHT);
            GeoIdHeight_IsNullOrEmpty(loc, geoidheight);
        }
    }

    private static void LocationHasAltAndSubtractAltOffsetIsNotNull(Location loc, PreferenceHelper ph) {
        if(loc.hasAltitude() && ph.getSubtractAltitudeOffset() != 0){
            loc.setAltitude(loc.getAltitude() - ph.getSubtractAltitudeOffset());
        }
    }

    private static void GeoIdHeight_IsNullOrEmpty(Location loc, String geoidheight) {
        if (!Strings.isNullOrEmpty(geoidheight)) {
            loc.setAltitude((float) loc.getAltitude() - Float.valueOf(geoidheight));
        }
        else {
            //If geoid height not present for adjustment, don't record an elevation at all.
            loc.removeAltitude();
        }
    }
}
