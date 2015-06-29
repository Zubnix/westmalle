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

import com.google.auto.factory.AutoFactory;
import org.freedesktop.wayland.server.WlOutputResource;

import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "OutputFactory")
public class Output {

    @Nonnull
    private       OutputGeometry outputGeometry;
    @Nonnull
    private       OutputMode     outputMode;
    @Nonnull
    private final Object         outputImplementation;

    Output(@Nonnull final OutputGeometry outputGeometry,
           @Nonnull final OutputMode outputMode,
           @Nonnull final Object outputImplementation) {
        this.outputGeometry = outputGeometry;
        this.outputMode = outputMode;
        this.outputImplementation = outputImplementation;
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputGeometry outputGeometry) {
        //TODO unit test
        this.outputGeometry = outputGeometry;
        resources.forEach(this::notifyGeometry);
        return this;
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputMode outputMode) {
        //TODO unit test
        this.outputMode = outputMode;
        resources.forEach(this::notifyMode);
        return this;
    }

    public Output notifyMode(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.mode(this.outputMode.getFlags(),
                              this.outputMode.getWidth(),
                              this.outputMode.getHeight(),
                              this.outputMode.getRefresh());
        return this;
    }

    public Output notifyGeometry(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.geometry(this.outputGeometry.getX(),
                                  this.outputGeometry.getY(),
                                  this.outputGeometry.getPhysicalWidth(),
                                  this.outputGeometry.getPhysicalHeight(),
                                  this.outputGeometry.getSubpixel(),
                                  this.outputGeometry.getMake(),
                                  this.outputGeometry.getModel(),
                                  this.outputGeometry.getTransform());
        return this;
    }

    @Nonnull
    public OutputMode getMode() {
        return this.outputMode;
    }

    @Nonnull
    public OutputGeometry getGeometry() {
        return this.outputGeometry;
    }

    @Nonnull
    public Object getImplementation() {
        return this.outputImplementation;
    }
}