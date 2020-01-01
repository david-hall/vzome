
package com.vzome.core.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.RealVector;
import com.vzome.opengl.InstancedGeometry;

/**
* Should this be merged with RenderedModel?
*/
public class ShapeAndInstances implements InstancedGeometry
{
    private FloatBuffer mInstancePositions;
    private FloatBuffer mVertices;
    private FloatBuffer mNormals = null;
    int positionsVBO = -1, normalsVBO = -1, verticesVBO = -1;
    private boolean usesVBOs = false;
    private float[] mColor = new float[4];
    private int instanceCount = 0;
    private int vertexCount;
    private final float globalScale;
    
    private Set<RenderedManifestation> instances = new HashSet<>();  // needed when we remove something

    public static final int COORDS_PER_VERTEX = 3;

    public ShapeAndInstances( Polyhedron shape, Color color, float globalScale )
    {
        this.globalScale = globalScale;
        color .getRGBColorComponents( this .mColor );

        List<RealVector> vertices = new ArrayList<>();
        List<RealVector> normals = new ArrayList<>();
        List<AlgebraicVector> vertexList = shape.getVertexList();
        Set<Polyhedron.Face> faces = shape.getFaceSet();
        for (Polyhedron.Face face : faces) {
            AlgebraicVector normal = face.getNormal();
            RealVector rn = normal.negate().toRealVector().normalize();
            int count = face.size();
            AlgebraicVector vertex = vertexList.get(face.getVertex(0));
            RealVector rv0 = vertex.toRealVector();
            vertex = vertexList.get(face.getVertex(1));
            RealVector rvPrev = vertex.toRealVector();
            for (int i = 2; i < count; i++) {
                vertex = vertexList.get(face.getVertex(i));
                RealVector rv = vertex.toRealVector();
                vertices.add(rv0);
                normals.add(rn);
                vertices.add(rvPrev);
                normals.add(rn);
                vertices.add(rv);
                normals.add(rn);
                rvPrev = rv;
            }
        }

        float[] verticesArray = new float[ShapeAndInstances.COORDS_PER_VERTEX * vertices.size()];
        float[] normalsArray = new float[ShapeAndInstances.COORDS_PER_VERTEX * normals.size()];  // same size!
        for (int i = 0; i < vertices.size(); i++) {
            RealVector vector = vertices.get(i);
            vector = vector .scale( globalScale );
            verticesArray[i * ShapeAndInstances.COORDS_PER_VERTEX] = (float) vector.x;
            verticesArray[i * ShapeAndInstances.COORDS_PER_VERTEX + 1] = (float) vector.y;
            verticesArray[i * ShapeAndInstances.COORDS_PER_VERTEX + 2] = (float) vector.z;
            vector = normals.get(i);
            normalsArray[i * ShapeAndInstances.COORDS_PER_VERTEX] = (float) vector.x;
            normalsArray[i * ShapeAndInstances.COORDS_PER_VERTEX + 1] = (float) vector.y;
            normalsArray[i * ShapeAndInstances.COORDS_PER_VERTEX + 2] = (float) vector.z;
        }

        this .vertexCount = verticesArray.length / COORDS_PER_VERTEX;

        ByteBuffer bbVertices = ByteBuffer.allocateDirect( verticesArray.length * 4 );
        bbVertices.order(ByteOrder.nativeOrder());
        this .mVertices = bbVertices.asFloatBuffer();
        this .mVertices.put( verticesArray );
        this .mVertices.position(0);

        if ( normalsArray != null ) {
            ByteBuffer bbNormals = ByteBuffer.allocateDirect( normalsArray.length * 4 );
            bbNormals.order(ByteOrder.nativeOrder());
            this .mNormals = bbNormals.asFloatBuffer();
            this .mNormals .put(normalsArray);
            this .mNormals .position(0);
        }
    }

    public void setBuffers( int buffer, int buffer1, int buffer2 )
    {
        this .verticesVBO = buffer;
        this .normalsVBO = buffer1;
        this .positionsVBO = buffer2;
        this .mVertices = null;
        this .mInstancePositions = null;
        this .mNormals = null;
        this .usesVBOs = true;
    }

    public void setShapeData( float[] verticesArray, float[] normalsArray, float[] color )
    {
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getVertices()
     */
    @Override
    public FloatBuffer getVertices()
    {
        return this .mVertices;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getNormals()
     */
    @Override
    public FloatBuffer getNormals()
    {
        return this .mNormals;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getPositions()
     */
    @Override
    public FloatBuffer getPositions()
    {
        return this .mInstancePositions;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getColor()
     */
    @Override
    public float[] getColor()
    {
        return mColor;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getVertexCount()
     */
    @Override
    public int getVertexCount()
    {
        return this .vertexCount;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getInstanceCount()
     */
    @Override
    public int getInstanceCount()
    {
        return this .instanceCount;
    }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#usesVBOs()
     */
    @Override
    public boolean usesVBOs() { return this .usesVBOs; }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getVerticesVBO()
     */
    @Override
    public int getVerticesVBO() { return this .verticesVBO; }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getNormalsVBO()
     */
    @Override
    public int getNormalsVBO() { return this .normalsVBO; }

    /* (non-Javadoc)
     * @see com.vzome.opengl.InstancedGeometry#getPositionsVBO()
     */
    @Override
    public int getPositionsVBO() { return this .positionsVBO; }

    public void rebuildInstanceData()
    {
        float[] offsets = new float[4 * instances.size()];
        int i = 0;
        for( RenderedManifestation part : instances ) {
            AlgebraicVector vector = part .getLocationAV();
            if ( vector == null ) {
                // must be a panel
                vector = part .getModel() .getField() .origin( 3 );
            }
            int zone = part .getStrutZone();
            float orientationAndGlow = part .getGlow() + (float) zone;
            offsets[i * 4 + 0] = globalScale * (float) vector .getComponent( AlgebraicVector.X ) .evaluate();
            offsets[i * 4 + 1] = globalScale * (float) vector .getComponent( AlgebraicVector.Y ) .evaluate();
            offsets[i * 4 + 2] = globalScale * (float) vector .getComponent( AlgebraicVector.Z ) .evaluate();
            offsets[i * 4 + 3] = orientationAndGlow;
            ++i;
        }

        ByteBuffer bbOffsets = ByteBuffer.allocateDirect( offsets.length * 4 );
        bbOffsets.order(ByteOrder.nativeOrder());
        mInstancePositions = bbOffsets.asFloatBuffer();
        mInstancePositions.put( offsets );
        mInstancePositions.position(0);
        instanceCount = offsets.length / 4;
    }

    public void addInstance( RenderedManifestation rm )
    {
        this .instances .add( rm );
        this .rebuildInstanceData();
    }

    public void removeInstance( RenderedManifestation rm )
    {
        this .instances .remove( rm );
        this .rebuildInstanceData();
    }

    public Collection<RenderedManifestation> getInstances()
    {
        return this .instances;
    }

    public void removeInstances()
    {
        this .instances .clear();
        this .rebuildInstanceData();
    }
}
