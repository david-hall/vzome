package org.vorthmann.zome.app.impl;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.vzome.core.viewing.Camera;
import com.vzome.desktop.controller.CameraController;

/**
 * A CameraController.Viewer that only changes rotations.
 * 
 * It is connected to the Camera to rotate the trackball.
 * 
 * @author vorth
 *
 */
public class TrackballRenderingViewer implements CameraController.Viewer
{
	private final CameraController.Viewer delegate;
	
	private final Vector3d translation;

	public TrackballRenderingViewer( CameraController.Viewer delegate )
	{
		this .delegate = delegate;
		
		this .translation = new Vector3d();
		Matrix4d matrix = new Matrix4d();
		Camera defaultCamera = new Camera();
		defaultCamera .setMagnification( 1.0f );
		defaultCamera .getViewTransform( matrix );
		matrix .get( translation ); // save the default translation to apply on every update below

		// set the perspective view just once
		double near = defaultCamera .getNearClipDistance();
        double far = defaultCamera .getFarClipDistance();
        double fov = defaultCamera .getFieldOfView();
		this .delegate .setPerspective( fov, 1.0d, near, far );
	}

	@Override
	public void setViewTransformation( Matrix4d trans )
	{
	    Matrix3d justRotation3d = new Matrix3d();
	    trans .get( justRotation3d );
	    justRotation3d .invert(); // to match the invert() in the caller
	    Matrix4d finalTransform = new Matrix4d();
	    finalTransform .set( this .translation );
	    finalTransform .setRotation( justRotation3d );
	    finalTransform .invert(); // to match the invert() in the caller
	    this .delegate .setViewTransformation( finalTransform );
	}

	@Override
	public void setPerspective( double fov, double aspectRatio, double near, double far ) {}

	@Override
	public void setOrthographic( double halfEdge, double near, double far ) {}
}
