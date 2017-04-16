package edu.thunderseethe.rapulazer;

import android.content.res.AssetManager;
import android.opengl.*;
import android.util.Log;
import android.content.*;

import java.io.*;
import java.nio.*;
import java.nio.IntBuffer;
import java.util.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class VisualizerGLRenderer implements GLSurfaceView.Renderer {

    private DataRef<AudioFeatures> mDataRef;

    public VisualizerGLRenderer(Context context, DataRef<AudioFeatures> mDataRef) {
        this.mContext = context;
        this.mDataRef = mDataRef;
    }

    public void updateRef(DataRef<AudioFeatures> _dataRef) {
        mDataRef = _dataRef;
    }

    private static final int FLOAT_BYTES = 4;

    class Cube {

        private IntBuffer mVbo, mVao;
        private FloatBuffer mVboBuffer;
        private ByteBuffer  mIboBuffer;
        private float x=-1, y=-1, z=-1, width=2, height=2, depth=2;

        private float vbo_arr[] = {
                x, y, z, 1, 0, 0,
                x+width, y, z, 0, 1, 0,
                x+width, y+height, z, 0, 0, 1,
                x, y+height, z, 0, 0.5f, 0.5f,
                x, y, z+depth, 0, 0, 1,
                x+width, y, z+depth, 0.5f, 0, 0.5f,
                x+width, y+height, z+depth, 1, 0, 0,
                x, y+height, z+depth, 1, 0, 0,
        };

        private byte ibo_arr[] = {
                0, 1, 3,
                1, 2, 3,
                2, 3, 7,
                2, 7, 6,
                4, 5, 7,
                5, 6, 7,
                4, 5, 1,
                1, 0, 4,
                2, 6, 5,
                1, 2, 5,
                4, 3, 7,
                0, 3, 4
        };

        public Cube() {
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vbo_arr.length * FLOAT_BYTES);
            byteBuf.order(ByteOrder.nativeOrder());
            mVboBuffer = byteBuf.asFloatBuffer();
            mVboBuffer.put(vbo_arr);
            mVboBuffer.position(0);

            mIboBuffer = ByteBuffer.allocateDirect(ibo_arr.length);
            mIboBuffer.put(ibo_arr);
            mIboBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(4);
            byteBuf.order(ByteOrder.nativeOrder());
            mVao = byteBuf.asIntBuffer();
            GLES30.glGenVertexArrays(1, mVao);
            mVao.position(0);

            byteBuf = ByteBuffer.allocateDirect(8);
            byteBuf.order(ByteOrder.nativeOrder());
            mVbo = byteBuf.asIntBuffer();
            GLES30.glGenBuffers(2, mVbo);
            mVbo.position(0);

            // Bind the Vertex Array Object
            GLES30.glBindVertexArray(mVao.get(0));

            // Bind the Vertex Buffer Object
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, mVbo.get(1) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ELEMENT_ARRAY_BUFFER, mIboBuffer.capacity(), mIboBuffer, GLES30.GL_STATIC_DRAW );
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );

            // Bind the vertices
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, mVbo.get(0) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, mVboBuffer.capacity()*4, mVboBuffer, GLES30.GL_STATIC_DRAW );

            // Set attributes
            GLES30.glEnableVertexAttribArray( 0 );
            GLES30.glVertexAttribPointer( 0, 3, GLES20.GL_FLOAT, false, 6*FLOAT_BYTES, 3*FLOAT_BYTES );
            GLES30.glEnableVertexAttribArray( 1 );
            GLES30.glVertexAttribPointer( 1, 3, GLES20.GL_FLOAT, false, 6*FLOAT_BYTES, 0 );

            // Clear buffers
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, 0 );
            GLES30.glBindVertexArray( 0 );
        }

        public void draw(GL10 gl) {
            mIboBuffer.position(0);
            mVboBuffer.position(0);

            /* Draw what we just bound the GPU */

            // Bind to the VAO that has all the information about the vertices
            GLES30.glBindVertexArray( mVao.get(0) );
            GLES30.glEnableVertexAttribArray( 0 );
            GLES30.glEnableVertexAttribArray( 1 );

            // Bind to the index VBO that has all the information about the order of the vertices
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, mVbo.get(1) );

            // Draw everything
            GLES30.glDrawElements( GLES11.GL_TRIANGLES, ibo_arr.length, GLES20.GL_UNSIGNED_BYTE, 0 );

            // Put everything back to default (deselect)
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );
            GLES30.glDisableVertexAttribArray( 0 );
            GLES30.glDisableVertexAttribArray( 1 );
            GLES30.glBindVertexArray( 0 );
        }
    }

    private final float[] mVMatrix = new float[16];
    private final float[] mPMatrix = new float[16];
    private final float[] mMMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final Context mContext;
    private final Random rn_jesus = new Random();

    private int mProgram = -1;
    private int muPMatrixHandle = -1;
    private int muVMatrixHandle = -1;
    private int muMMatrixHandle = -1;

    private float set_fps = 60;
    private float running_bpm_count = 0;
    private float set_bpm = 120;
    private long last_start_time = 0;
    private float scale = 0;
    private float angle = 0;
    private float rotation = 0;
    private float shake_dx = 0;
    private float shake_dy = 0;

    private Cube mCube;

    private boolean compileShaders() {
        // Read shader files
        StringBuilder vertex_code_builder = new StringBuilder();
        StringBuilder fragment_code_builder = new StringBuilder();
        try{
            String line;
            AssetManager am = mContext.getAssets();
            BufferedReader vert_in = new BufferedReader( new InputStreamReader( am.open("visualizer.vert") ) );
            while( (line = vert_in.readLine()) != null ) {
                vertex_code_builder.append(line);
                vertex_code_builder.append('\n');
            }
            vert_in.close();

            BufferedReader frag_in = new BufferedReader( new InputStreamReader( am.open("visualizer.frag") ) );
            while( (line = frag_in.readLine()) != null ) {
                fragment_code_builder.append(line);
                fragment_code_builder.append('\n');
            }
            frag_in.close();
        } catch ( IOException e ) {
            Log.wtf("Shader Compiling", e);
            return false;
        }
        String vertex_code = vertex_code_builder.toString();
        String fragment_code = fragment_code_builder.toString();

        // Create and compile vertex code
        int vertex_shader = GLES20.glCreateShader( GLES20.GL_VERTEX_SHADER );
        GLES20.glShaderSource(vertex_shader, vertex_code);
        GLES20.glCompileShader(vertex_shader);

        // Check for compile errors
        IntBuffer is_compiled = IntBuffer.allocate(1);
        GLES20.glGetShaderiv(vertex_shader, GLES20.GL_COMPILE_STATUS, is_compiled);
        if( is_compiled.get(0) == GL10.GL_FALSE ) {
            // Print compile warnings
            String log = GLES20.glGetShaderInfoLog(vertex_shader);
            GLES20.glDeleteShader(vertex_shader);
            Log.w("Shader Compiling", log);
            return false;
        }

        // Create and compile fragment code
        int fragment_shader = GLES20.glCreateShader( GLES20.GL_FRAGMENT_SHADER );
        GLES20.glShaderSource(fragment_shader, fragment_code);
        GLES20.glCompileShader(fragment_shader);

        // Check for compile errors
        GLES20.glGetShaderiv(fragment_shader, GLES20.GL_COMPILE_STATUS, is_compiled);
        if( is_compiled.get(0) == GL10.GL_FALSE ) {
            // Print compile warnings
            String log = GLES20.glGetShaderInfoLog(fragment_shader);
            GLES20.glDeleteShader(vertex_shader);
            GLES20.glDeleteShader(fragment_shader);
            Log.w("Shader Compiling", log);
            return false;
        }

        // Create new shader program
        mProgram = GLES20.glCreateProgram();
        if( mProgram == 0 ) {
            Log.e("Shader Compiling", "Could not create a new shader program.");
            return false;
        }

        // Attach shaders to program
        GLES20.glAttachShader(mProgram, vertex_shader);
        GLES20.glAttachShader(mProgram, fragment_shader);

        // Link the program
        GLES20.glLinkProgram(mProgram);

        // Check for linking errors
        IntBuffer is_linked = IntBuffer.allocate(1);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, is_linked);
        if( is_linked.get(0) == GL10.GL_FALSE ) {
            // Print compile warnings
            String log = GLES20.glGetProgramInfoLog(mProgram);
            GLES20.glDeleteProgram(mProgram);
            GLES20.glDeleteShader(vertex_shader);
            GLES20.glDeleteShader(fragment_shader);
            Log.w("Shader Compiling", log);
            return false;
        }

        // Validate the program
        GLES20.glValidateProgram(mProgram);

        // Detach shaders
        GLES20.glDetachShader(mProgram, vertex_shader);
        GLES20.glDetachShader(mProgram, fragment_shader);

        return true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] vers = new int[2];
        GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, vers, 0);
        GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, vers, 1);
        Log.d("OpenGL Version", vers[0]+"."+vers[1]);
        Log.d("Shader Version", GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION));

        // Config OpenGL
        GLES11.glEnable( GLES11.GL_DEPTH_TEST );
        GLES11.glDepthFunc( GLES11.GL_LEQUAL );
        GLES11.glEnable( GLES11.GL_BLEND );
        GLES11.glBlendFunc( GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA );

        // Deal with shader shit
        compileShaders();
        GLES30.glUseProgram( mProgram );
        GLES30.glBindAttribLocation( mProgram, 0, "in_position" );
        GLES30.glBindAttribLocation( mProgram, 1, "in_color" );

        mCube = new Cube();

        // Create a camera view matrix
        muPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "projection_matrix");
        muVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "view_matrix");
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "model_matrix");

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 30, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, mVMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // create a projection matrix from device screen geometry
        Matrix.perspectiveM(mPMatrix, 0, 40.0f, ratio, 0.1f, 10000.0f);
        GLES20.glUniformMatrix4fv(muPMatrixHandle, 1, false, mPMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Timing control
        long delta_time = 0;
        if( last_start_time != 0 ) {
            delta_time = System.nanoTime() - last_start_time;
            long desired_time = (long) ((double)1e9 / set_fps);
            long desired_break = desired_time - delta_time;
            if( desired_break > 0 ) {
                try{
                    Thread.sleep( (int) (desired_break/1e6), (int) (desired_break%1e6) );
                }catch( InterruptedException e ){
                    Log.wtf("Bad things", e);
                }
            }
        }
        delta_time = System.nanoTime() - last_start_time;
        last_start_time = System.nanoTime();

        // Compute transformations
        if( delta_time != 0 ) {
            if( mDataRef.data() != null && mDataRef.data().is_beat ) {
                running_bpm_count += Math.PI/5;
                float angle = (float) (2*Math.PI*rn_jesus.nextFloat());
                float power = 0.5f * rn_jesus.nextFloat();
                shake_dx += power * Math.cos(angle);
                shake_dy += power * Math.sin(angle);
            }
            if( running_bpm_count > 0 ) {
                running_bpm_count -= set_bpm * Math.PI * (delta_time / 60e9);
            }
            if( shake_dx > 0 ) {
                shake_dx /= 4;
            }
            if( shake_dx < 0 ) {
                shake_dx /= 4;
            }
            if( shake_dy > 0 ) {
                shake_dy /= 4;
            }
            if( shake_dy < 0 ) {
                shake_dy /= 4;
            }
            scale = (float) (0.8 + 0.2*Math.abs(Math.sin(running_bpm_count)));
            rotation = (float) (13.0*Math.sin(running_bpm_count));
        }

        // Transform the scene
        Matrix.setIdentityM(mMMatrix, 0);
        Matrix.translateM(mMMatrix, 0, shake_dx, shake_dy, 0);
//        Matrix.rotateM(mMMatrix, 0, rotation, 0, 1, 0);

        float sections = 4;
        float scene_width = 10;
        float scene_height = 10;
        float left = -scene_width/2;
        float right = scene_width/2;
        float top = -scene_height/2;
        float bottom = scene_height/2;
        float step_x = scene_width/sections;
        float step_y = scene_height/sections;

        // Draw objects
        float flash = (float) (Math.min(running_bpm_count, 2.0f*Math.PI) / 50.0f*Math.PI);
        gl.glClearColor(flash, flash, flash, 1);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        for( float tx=left ; tx<=right ; tx+=step_x ) {
            Matrix.translateM(mMMatrix, 0, tx, 0, 0);
            for( float ty=top ; ty<=bottom ; ty+=step_y ) {
                Matrix.translateM(mMMatrix, 0, 0, ty, 0);
                Matrix.scaleM(mMMatrix, 0, scale, scale, scale);
                GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, mMMatrix, 0);
                Matrix.scaleM(mMMatrix, 0, 1/scale, 1/scale, 1/scale);
                Matrix.translateM(mMMatrix, 0, 0, -ty, 0);
                mCube.draw(gl);
            }
            Matrix.translateM(mMMatrix, 0, -tx, 0, 0);
        }

        // Check for errors
        int err = gl.glGetError();
        if( err != GLES10.GL_NO_ERROR ) {
            Log.w("OpenGL Error", "Error Code:"+err);
        }

        gl.glFinish();
    }
}