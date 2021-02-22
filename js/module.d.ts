/*
 * Copyright (c) 2021, Unicon Creation BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import clamp from './lib/clamp';
import equals from './lib/equals';
import naturalOrder from './lib/naturalOrder';
import scrollToElement from './lib/scrollToElement';
import { difference as moduloDifference, nearest as moduloNearest } from './lib/Modulo';

import ArrayTools from './lib/com.numdata.oss/ArrayTools';
import AugmentedList from './src/com.numdata.oss/AugmentedList';
import AugmentedArrayList from './lib/com.numdata.oss/AugmentedArrayList';
import DecimalFormat from './lib/com.numdata.oss/DecimalFormat';
import DecimalFormatSymbols from './lib/com.numdata.oss/DecimalFormatSymbols';
import Glob from './lib/com.numdata.oss/Glob';
import LengthMeasureFormat from './lib/com.numdata.oss/LengthMeasureFormat';
import LengthMeasurePreferences from './lib/com.numdata.oss/LengthMeasurePreferences';
import Locale from './lib/com.numdata.oss/Locale';
import LocalizedString from './lib/com.numdata.oss/LocalizedString';
import NestedProperties from './lib/com.numdata.oss/NestedProperties';
import signum from './lib/com.numdata.oss/signum';
import toDegrees from './lib/com.numdata.oss/toDegrees';
import toRadians from './lib/com.numdata.oss/toRadians';

import EventDispatcher from './lib/com.numdata.oss.event/EventDispatcher';
import ClassLogger from './lib/com.numdata.oss.log/ClassLogger';
import Enum from './lib/java.lang/Enum';

export {
    clamp,
    equals,
    naturalOrder,
    scrollToElement,
    moduloDifference,
    moduloNearest,

    ArrayTools,
    AugmentedList,
    AugmentedArrayList,
    DecimalFormat,
    DecimalFormatSymbols,
    Glob,
    LengthMeasureFormat,
    LengthMeasurePreferences,
    Locale,
    LocalizedString,
    NestedProperties,
    signum,
    toDegrees,
    toRadians,

    EventDispatcher,
    ClassLogger,
    Enum
};
