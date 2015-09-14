//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;

public class Subsurface implements Role {

    @Nonnull
    private final WlSurfaceResource  parentWlSurfaceResource;
    @Nonnull
    private final WlSurfaceResource  wlSurfaceResource;
    @Nonnull
    private final Slot<SurfaceState> parentCommitSlot;
    @Nonnull
    private       Point              position;

    Subsurface(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
               @Nonnull final WlSurfaceResource wlSurfaceResource,
               @Nonnull final Slot<SurfaceState> parentCommitSlot,
               @Nonnull final Point position) {
        this.parentWlSurfaceResource = parentWlSurfaceResource;
        this.wlSurfaceResource = wlSurfaceResource;
        this.parentCommitSlot = parentCommitSlot;
        this.position = position;
    }

    public void setPosition(final int x,
                            final int y) {
        final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();
        this.position = parentSurface.global(Point.create(x,
                                                          y));
    }

    @Override
    public void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        if (parentSurface.getCommitSignal()
                         .isConnected(this.parentCommitSlot)) {
            //TODO sync mode. cache surface states until parent commits.

        }
        else {
            //desync mode. commit as usual but keep position relative to parent
            surface.setPosition(this.position);
        }
    }

    public void setSync() {
        final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();
        parentSurface.getCommitSignal()
                     .connect(this.parentCommitSlot);
    }

    public void setDesync() {
        final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();
        parentSurface.getCommitSignal()
                     .disconnect(this.parentCommitSlot);
    }
}
