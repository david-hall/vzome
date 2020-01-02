
//(c) Copyright 2014, Scott Vorthmann.

package org.vorthmann.zome.render.jogl;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.geom.AABBox;
import com.jogamp.opengl.util.FPSAnimator;
import com.vzome.core.render.RenderedManifestation;
import com.vzome.core.render.RenderingChanges;
import com.vzome.desktop.controller.RenderingViewer;
import com.vzome.opengl.RenderingProgram;

/**
 * A lower-level, and hopefully more performant alternative to Java3dRenderingViewer.
 * 
 * Note that even JOGL is behind the times, still supporting immediate mode:
 * 
 *   https://www.khronos.org/opengl/wiki/Legacy_OpenGL
 *   
 * I have not found any modern tutorials for JOGL, so I'm working from
 * 
 *   http://www.opengl-tutorial.org/
 * 
 * @author vorth
 *
 */
public class JoglRenderingViewer implements RenderingViewer, GLEventListener
{
    private final JoglScene scene;
    private JoglOpenGlShim glShim;
    private RenderingProgram renderer = null;
    private FPSAnimator animator;

    private float near = 100, far = 2000, fovX = 0.5f;
    private float[] projection = new float[16];
    private float[] modelView = new float[16]; // stored in column-major order, for JOGL-friendliness
    private float aspectRatio = 1f;
    private float halfEdgeX;

    JoglRenderingViewer( JoglScene scene, GLCanvas canvas )
    {
        this .scene = scene;

        if ( canvas == null )
            return;

        canvas .addGLEventListener( this );

        // Start animator (which should be a field).
        this .animator = new FPSAnimator( canvas, 60 );
        this .animator .start();
    }

    // Private methods
    
    private void render()
    {
        if ( this .renderer == null )
            return; // still initializing
        
        this .renderer .setView( this.modelView, projection );
        this .scene .render( this .renderer );
    }
    
    private void setProjection()
    {
        if ( this .fovX == 0f ) {
            float halfEdgeY = this .halfEdgeX / this .aspectRatio;
            FloatUtil .makeOrtho( projection, 0, true, -halfEdgeX, halfEdgeX, -halfEdgeY, halfEdgeY, this .near, this .far );
        } else {
            // The Camera model is set up for fovX, but FloatUtil.makePerspective wants fovY
            float fovY = this .fovX / this .aspectRatio;
            FloatUtil .makePerspective( projection, 0, true, fovY, this .aspectRatio, this .near, this .far );
        }
        this .render();
    }

    // These are GLEventListener methods

    @Override
    public void init( GLAutoDrawable drawable )
    {
        this .glShim = new JoglOpenGlShim( drawable .getGL() .getGL2() );
        this .renderer = new RenderingProgram( this .glShim );
        // store the scene geometry
    }

    @Override
    public void dispose( GLAutoDrawable drawable )
    {
        this .animator .stop();
    }

    @Override
    public void display( GLAutoDrawable drawable )
    {
        if ( this .glShim .isSameContext( drawable .getGL() .getGL2() ) ) {
            this .render();
        }
        else
            System.out.println( "Different GL2!" );
    }

    // These are RenderingViewer methods
    
    @Override
    public void setEye( int eye ) {}

    @Override
    public void setViewTransformation( Matrix4d trans, int eye )
    {
        Matrix4d copy = new Matrix4d();
        copy .invert( trans );
        int i = 0;
        // JOGL requires column-major order
        for ( int column = 0; column < 4; column++ )
            for ( int row = 0; row < 4; row++ )
            {
                this .modelView[ i ] = (float) copy .getElement( row, column );
                ++i;
            }
        this .render();
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
    {
        this .aspectRatio = (float) width / (float) height;
        this .setProjection();
    }

    @Override
    public void setPerspective( double fovX, double aspectRatio, double near, double far )
    {
        this .fovX = (float) fovX;
        this .near = (float) near;
        this .far = (float) far;
        this .setProjection();
    }

    @Override
    public void setOrthographic( double halfEdgeX, double near, double far )
    {
        this .halfEdgeX = (float) halfEdgeX;
        this .fovX = 0;
        this .near = (float) near;
        this .far = (float) far;
        this .setProjection();
    }

    @Override
    public RenderedManifestation pickManifestation( MouseEvent e )
    {
        int mouseX = e .getX();
        int mouseY = e .getY();
        Component canvas = e .getComponent();
        
        Ray ray = new Ray();
        FloatUtil .mapWinToRay(
                (float) mouseX, (float) (canvas .getHeight() - mouseY - 1), 0.1f, 0.3f,
                this.modelView, 0,
                this.projection, 0,
                new int[] { 0, 0, canvas .getWidth(), canvas .getHeight() }, 0,
                ray,
                new float[16], new float[16], new float[4] );
        System.out.println( "ray.orig = " + ray.orig[0] + " " + ray.orig[1] + " " + ray.orig[2]  );
        System.out.println( "ray.dir = " + ray.dir[0] + " " + ray.dir[1] + " " + ray.dir[2]  );

        // The scene will loop over all RMs.  Rendered balls will support the RM.isHit(intersector)
        //   method.  We return the first one hit, for now.
        return this .scene .pick( new RenderedManifestation.Intersector()
        {
            private final float[] dpyTmp1V3 = new float[3];
            private final float[] dpyTmp2V3 = new float[3];
            private final float[] dpyTmp3V3 = new float[3];

            @Override
            public boolean intersectAABBox( float[] min, float[] max )
            {
//                VectorUtil .scaleVec3( min, min, SCALE );
//                VectorUtil .scaleVec3( max, max, SCALE );
                AABBox sbox = new AABBox( min, max );
                float[] result = new float[3];
                if( sbox.intersectsRay(ray) ) {
                    if( null == sbox .getRayIntersection( result, ray, FloatUtil.EPSILON, true, dpyTmp1V3, dpyTmp2V3, dpyTmp3V3 ) ) {
                        System.out.println( "Failure to getRayIntersection" );
                    } else
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public Collection<RenderedManifestation> pickCube()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pickPoint( MouseEvent e, Point3d imagePt, Point3d eyePt )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public RenderingChanges getRenderingChanges()
    {
        return this .scene;
    }

    @Override
    public void captureImage( int maxSize, ImageCapture capture )
    {
        // TODO Auto-generated method stub
    }
}
