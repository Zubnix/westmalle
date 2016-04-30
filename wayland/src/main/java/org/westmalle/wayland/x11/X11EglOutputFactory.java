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
package org.westmalle.wayland.x11;

import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.EglGetPlatformDisplayEXT;
import org.westmalle.wayland.nativ.libEGL.LibEGL;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

import static org.freedesktop.jaccall.Pointer.malloc;
import static org.freedesktop.jaccall.Size.sizeof;
import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BACK_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BUFFER_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_COLOR_BUFFER_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_DEPTH_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_PLATFORM_X11_KHR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDER_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RGB_BUFFER;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SAMPLES;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SAMPLE_BUFFERS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_STENCIL_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;

public class X11EglOutputFactory {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final LibEGL                     libEGL;
    @Nonnull
    private final PrivateX11EglOutputFactory privateX11EglOutputFactory;

    @Inject
    X11EglOutputFactory(@Nonnull final LibEGL libEGL,
                        @Nonnull final PrivateX11EglOutputFactory privateX11EglOutputFactory) {
        this.libEGL = libEGL;
        this.privateX11EglOutputFactory = privateX11EglOutputFactory;
    }

    @Nonnull
    public X11EglOutput create(final long display,
                               final int window) {
        if (this.libEGL.eglBindAPI(EGL_OPENGL_ES_API) == 0L) {
            throw new RuntimeException("eglBindAPI failed");
        }
        final long eglDisplay = createEglDisplay(display);

        final int configs_size = 256 * sizeof((Pointer<?>) null);
        final Pointer<Pointer> configs = malloc(configs_size,
                                                Pointer.class);
        chooseConfig(eglDisplay,
                     configs,
                     configs_size);
        final long config = configs.dref().address;
        final long context = createEglContext(eglDisplay,
                                              config);

        return this.privateX11EglOutputFactory.create(eglDisplay,
                                                      createEglSurface(eglDisplay,
                                                                       config,
                                                                       context,
                                                                       window),
                                                      context);
    }

    private long createEglDisplay(final long nativeDisplay) {
        final Pointer<String> eglQueryString = Pointer.wrap(String.class,
                                                            this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                                                                       EGL_EXTENSIONS));


        if (eglQueryString.address == 0L || !eglQueryString.dref()
                                                           .contains("EGL_EXT_platform_x11")) {
            throw new RuntimeException("Required extension EGL_EXT_platform_x11 not available.");
        }

        final Pointer<EglGetPlatformDisplayEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglGetPlatformDisplayEXT.class,
                                                                                        this.libEGL.eglGetProcAddress(Pointer.nref("eglGetPlatformDisplayEXT").address));

        final long eglDisplay = eglGetPlatformDisplayEXT.dref()
                                                        .$(EGL_PLATFORM_X11_KHR,
                                                           nativeDisplay,
                                                           0L);
        if (eglDisplay == 0L) {
            throw new RuntimeException("eglGetDisplay() failed");
        }
        if (this.libEGL.eglInitialize(eglDisplay,
                                      0L,
                                      0L) == 0) {
            throw new RuntimeException("eglInitialize() failed");
        }

        final String eglClientApis = Pointer.wrap(String.class,
                                                  this.libEGL.eglQueryString(eglDisplay,
                                                                             LibEGL.EGL_CLIENT_APIS))
                                            .dref();
        final String eglVendor = Pointer.wrap(String.class,
                                              this.libEGL.eglQueryString(eglDisplay,
                                                                         LibEGL.EGL_VENDOR))
                                        .dref();
        final String eglVersion = Pointer.wrap(String.class,
                                               this.libEGL.eglQueryString(eglDisplay,
                                                                          LibEGL.EGL_VERSION))
                                         .dref();

        LOGGER.info(format("Creating X11 EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n"
                           + "\tEGL extensions: %s",
                           eglClientApis,
                           eglVendor,
                           eglVersion,
                           eglQueryString.dref()));

        return eglDisplay;
    }

    private void chooseConfig(final long eglDisplay,
                              final Pointer<Pointer> configs,
                              final int configs_size) {
        final Pointer<Integer> num_configs = Pointer.nref(0);
        final Pointer<Integer> egl_config_attribs = Pointer.nref(
                //@formatter:off
                 EGL_COLOR_BUFFER_TYPE, EGL_RGB_BUFFER,
                 EGL_BUFFER_SIZE,       32,
                 EGL_RED_SIZE,          8,
                 EGL_GREEN_SIZE,        8,
                 EGL_BLUE_SIZE,         8,
                 EGL_ALPHA_SIZE,        8,
                 EGL_DEPTH_SIZE,        24,
                 EGL_STENCIL_SIZE,      8,
                 EGL_SAMPLE_BUFFERS,    0,
                 EGL_SAMPLES,           0,
                 EGL_SURFACE_TYPE,      EGL_WINDOW_BIT,
                 EGL_RENDERABLE_TYPE,   EGL_OPENGL_ES2_BIT,
                 EGL_NONE
                //@formatter:on
                                                                );
        if (this.libEGL.eglChooseConfig(eglDisplay,
                                        egl_config_attribs.address,
                                        configs.address,
                                        configs_size,
                                        num_configs.address) == 0) {
            throw new RuntimeException("eglChooseConfig() failed");
        }
        if (num_configs.dref() == 0) {
            throw new RuntimeException("failed to find suitable EGLConfig");
        }
    }

    private long createEglContext(final long eglDisplay,
                                  final long config) {
        final Pointer<?> eglContextAttribs = Pointer.nref(
                //@formatter:off
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
                //@formatter:on
                                                         );
        final long context = this.libEGL.eglCreateContext(eglDisplay,
                                                          config,
                                                          EGL_NO_CONTEXT,
                                                          eglContextAttribs.address);
        if (context == 0L) {
            throw new RuntimeException("eglCreateContext() failed");
        }
        return context;
    }

    private long createEglSurface(final long eglDisplay,
                                  final long config,
                                  final long context,
                                  final int nativeWindow) {
        final Pointer<Integer> eglSurfaceAttribs = Pointer.nref(EGL_RENDER_BUFFER,
                                                                EGL_BACK_BUFFER,
                                                                EGL_NONE);

        final Pointer<EglCreatePlatformWindowSurfaceEXT> eglGetPlatformDisplayEXT = Pointer.wrap(EglCreatePlatformWindowSurfaceEXT.class,
                                                                                                 this.libEGL.eglGetProcAddress(Pointer.nref("eglCreatePlatformWindowSurfaceEXT").address));
        final long eglSurface = eglGetPlatformDisplayEXT.dref()
                                                        .$(eglDisplay,
                                                           config,
                                                           Pointer.nref(nativeWindow).address,
                                                           eglSurfaceAttribs.address);
        if (eglSurface == 0L) {
            throw new RuntimeException("eglCreateWindowSurface() failed");
        }
        if (this.libEGL.eglMakeCurrent(eglDisplay,
                                       eglSurface,
                                       eglSurface,
                                       context) == 0L) {
            throw new RuntimeException("eglMakeCurrent() failed");
        }
        return eglSurface;
    }
}
