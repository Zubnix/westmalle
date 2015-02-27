package org.westmalle.wayland.output.gl;

import com.google.common.util.concurrent.ListeningExecutorService;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.output.Surface;
import org.westmalle.wayland.protocol.WlSurface;

import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GLRenderEngineTest {

    @Mock
    private ListeningExecutorService renderThread;
    @Mock
    private GLAutoDrawable           drawable;
    @Mock
    private IntBuffer                elementBuffer;
    @Mock
    private IntBuffer                vertexBuffer;
    @InjectMocks
    private GLRenderEngine           glRenderEngine;

    @Test
    public void testBegin() throws Exception {
        //given
        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.drawable.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);
        //when
        this.glRenderEngine.begin();
        //then
        verify(this.renderThread,
               times(1)).submit((Runnable) any());
        //and when
        queue.get(0)
             .run();
        //then
        verify(gl2ES2).glClear(anyInt());
    }

    @Test
    public void testDraw() throws Exception {
        //given
        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.drawable.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);

        doAnswer(invocation -> {
            Object arg3 = invocation.getArguments()[2];
            final IntBuffer vstatus = (IntBuffer) arg3;
            vstatus.put(0,
                        GL.GL_TRUE);
            return null;
        }).when(gl2ES2)
          .glGetShaderiv(anyInt(),
                         eq(GL2ES2.GL_COMPILE_STATUS),
                         any());

        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = mock(WlSurface.class);
        when(surfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final PointImmutable position = new Point(-10,
                                                  45);
        when(surface.getPosition()).thenReturn(position);

        final ShmBuffer buffer = mock(ShmBuffer.class);
        when(buffer.getWidth()).thenReturn(100);
        when(buffer.getHeight()).thenReturn(250);

        this.glRenderEngine.begin();
        queue.get(0)
             .run();
        //when
        this.glRenderEngine.draw(surfaceResource,
                                 buffer);
        //then
        verify(this.renderThread,
               times(2)).submit((Runnable) any());
        //and when
        queue.get(1)
             .run();
        //then
        verify(gl2ES2).glLinkProgram(anyInt());
        verify(gl2ES2).glDrawElements(anyInt(),
                                      anyInt(),
                                      anyInt(),
                                      anyLong());
    }

    @Test
    public void testEnd() throws Exception {
        //given
        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.drawable.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);
        //when
        this.glRenderEngine.end();
        //then
        verify(this.renderThread,
               times(1)).submit((Runnable) any());
        //and when
        queue.get(0)
             .run();
        //then
        verify(this.drawable,
               times(1)).swapBuffers();
    }
}