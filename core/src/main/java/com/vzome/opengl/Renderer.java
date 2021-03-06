package com.vzome.opengl;

import com.vzome.core.render.SymmetryRendering;

public interface Renderer
{
    void setLights( float[][] lightDirections, float[][] lightColors, float[] ambientLight );

    void setView( float[] modelView, float[] projection, float near, float fogFront, float far, boolean perspective );

    void clear( float[] background );

    void renderSymmetry( SymmetryRendering symmetryRendering );
}