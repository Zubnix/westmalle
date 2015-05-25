package org.westmalle.wayland.output;

import com.google.common.util.concurrent.ListenableFuture;
import com.jogamp.opengl.GLDrawable;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.protocol.WlSurface;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ShmBuffer.class)
public class RendererTest {

    @Mock
    private RenderEngine renderEngine;
    @InjectMocks
    private Renderer renderer;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ShmBuffer.class);
    }

    @Test
    public void testRender() throws Exception {
        //given
        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = mock(WlSurface.class);
        when(surfaceResource.getImplementation()).thenReturn(wlSurface);

        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);

        final SurfaceState surfaceState = mock(SurfaceState.class);
        when(surface.getState()).thenReturn(surfaceState);

        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        when(surfaceState.getBuffer()).thenReturn(Optional.of(wlBufferResource));

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);

        final ListenableFuture listenableFuture = mock(ListenableFuture.class);
        when(this.renderEngine.draw(any(),
                                       any())).thenReturn(listenableFuture);
        //when
        this.renderer.render(surfaceResource);
        //then
        verify(this.renderEngine).draw(surfaceResource,
                                          wlBufferResource);
    }

    @Test
    public void testBeginRender() throws Exception {
        //given
        final GLDrawable       glDrawable       = mock(GLDrawable.class);
        final ListenableFuture listenableFuture = mock(ListenableFuture.class);
        when(this.renderEngine.begin(glDrawable)).thenReturn(listenableFuture);
        //when
        this.renderer.beginRender(glDrawable);
        //then
        verify(this.renderEngine).begin(glDrawable);
    }

    @Test
    public void testEndRender() throws Exception {
        //given
        final GLDrawable       glDrawable       = mock(GLDrawable.class);
        final ListenableFuture listenableFuture = mock(ListenableFuture.class);
        when(this.renderEngine.end(glDrawable)).thenReturn(listenableFuture);
        //when
        this.renderer.endRender(glDrawable);
        //then
        verify(this.renderEngine).end(glDrawable);
    }
}