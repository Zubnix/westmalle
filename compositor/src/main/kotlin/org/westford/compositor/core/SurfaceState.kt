/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core

import com.google.auto.value.AutoValue
import org.freedesktop.wayland.server.WlBufferResource
import org.westford.compositor.core.calc.Mat4
import javax.annotation.Nonnegative

@AutoValue abstract class SurfaceState {

    abstract val opaqueRegion: Region?

    abstract val inputRegion: Region?

    abstract val damage: Region?

    abstract val buffer: WlBufferResource?

    abstract val bufferTransform: Mat4

    abstract val deltaPosition: Point

    @get:Nonnegative abstract val scale: Int

    abstract fun toBuilder(): Builder

    @AutoValue.Builder interface Builder {
        fun opaqueRegion(wlRegionResource: Region?): Builder

        fun inputRegion(wlRegionResource: Region?): Builder

        fun damage(damage: Region?): Builder

        fun buffer(wlBufferResource: WlBufferResource?): Builder

        fun bufferTransform(bufferTransform: Mat4): Builder

        fun scale(scale: Int): Builder

        fun deltaPosition(point: Point): Builder

        fun build(): SurfaceState
    }

    companion object {

        internal fun builder(): Builder {
            return AutoValue_SurfaceState.Builder().opaqueRegion(null).inputRegion(null).damage(null).buffer(null).bufferTransform(Mat4.IDENTITY).deltaPosition(Point.ZERO).scale(1)
        }
    }
}