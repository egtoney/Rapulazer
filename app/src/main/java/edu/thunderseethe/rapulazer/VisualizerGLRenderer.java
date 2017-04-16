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
    private int MAX_COUNT = 0;

    public VisualizerGLRenderer(Context context, DataRef<AudioFeatures> mDataRef) {
        this.mContext = context;
        this.mDataRef = mDataRef;
    }

    public void updateRef(DataRef<AudioFeatures> _dataRef) {
        mDataRef = _dataRef;
    }
    public void setMaxCount(int n) {
        MAX_COUNT = n;
    }

    private static final int FLOAT_BYTES = 4;

    class Cube {

        private IntBuffer mVbo, mVao;
        private FloatBuffer mVboBuffer;
        private FloatBuffer mVboInstanceBuffer;
        private ByteBuffer  mIboBuffer;
        private float x=-0.5f, y=-0.5f, z=-0.5f, width=1, height=1, depth=1;

        private float vbo_arr[] = {
                x, y, z,// 1, 0, 0,
                x+width, y, z,// 0, 1, 0,
                x+width, y+height, z,// 0, 0, 1,
                x, y+height, z,// 0, 0.5f, 0.5f,
                x, y, z+depth,// 0, 0, 1,
                x+width, y, z+depth,// 0.5f, 0, 0.5f,
                x+width, y+height, z+depth,// 1, 0, 0,
                x, y+height, z+depth,// 1, 0, 0,
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

            byteBuf = ByteBuffer.allocateDirect(100000 * FLOAT_BYTES);
            byteBuf.order(ByteOrder.nativeOrder());
            mVboInstanceBuffer = byteBuf.asFloatBuffer();
            mVboInstanceBuffer.position(0);

            mIboBuffer = ByteBuffer.allocateDirect(ibo_arr.length);
            mIboBuffer.put(ibo_arr);
            mIboBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(4);
            byteBuf.order(ByteOrder.nativeOrder());
            mVao = byteBuf.asIntBuffer();
            GLES30.glGenVertexArrays(1, mVao);
            mVao.position(0);

            byteBuf = ByteBuffer.allocateDirect(12);
            byteBuf.order(ByteOrder.nativeOrder());
            mVbo = byteBuf.asIntBuffer();
            GLES30.glGenBuffers(3, mVbo);
            mVbo.position(0);

            // Bind the Vertex Array Object
            GLES30.glBindVertexArray(mVao.get(0));

            /* ------------------------------------ */
            // Bind the Vertex Buffer Object
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, mVbo.get(1) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ELEMENT_ARRAY_BUFFER, mIboBuffer.capacity(), mIboBuffer, GLES30.GL_STATIC_DRAW );
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );

            /* ------------------------------------ */
            // Bind the vertices
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, mVbo.get(0) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, mVboBuffer.capacity()*4, mVboBuffer, GLES30.GL_STATIC_DRAW );

            // Set attributes
            GLES30.glEnableVertexAttribArray( 0 );
            GLES30.glVertexAttribPointer( 0, 3, GLES20.GL_FLOAT, false, 0, 0 );

            /* ------------------------------------ */
            // Bind the offsets
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, mVbo.get(2) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, mVboInstanceBuffer.capacity()*4, mVboInstanceBuffer, GLES30.GL_DYNAMIC_DRAW );

            // Set attributes
            GLES30.glEnableVertexAttribArray( 1 );
            GLES30.glVertexAttribPointer( 1, 3, GLES20.GL_FLOAT, false, 6*FLOAT_BYTES, 3*FLOAT_BYTES );
            GLES30.glVertexAttribDivisor( 1, 1 );
            GLES30.glEnableVertexAttribArray( 2 );
            GLES30.glVertexAttribPointer( 2, 3, GLES20.GL_FLOAT, false, 6*FLOAT_BYTES, 0 );
            GLES30.glVertexAttribDivisor( 2, 1 );

            /* ------------------------------------ */
            // Clear buffers
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, 0 );
            GLES30.glBindVertexArray( 0 );
        }

        public void draw(GL10 gl, float[] vbo_offset_arr) {
            /* Update the offset data */
            mVboInstanceBuffer.put(vbo_offset_arr);
            mVboInstanceBuffer.position(0);

            // Bind the offsets
            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, mVbo.get(2) );

            // Bind the data to the VBO
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, vbo_offset_arr.length*4, mVboInstanceBuffer, GLES30.GL_DYNAMIC_DRAW );

            /* ------------------------------------ */
            /* Draw what we just bound the GPU */
            mIboBuffer.position(0);
            mVboBuffer.position(0);

            // Bind to the VAO that has all the information about the vertices
            GLES30.glBindVertexArray( mVao.get(0) );
            GLES30.glEnableVertexAttribArray( 0 );
            GLES30.glEnableVertexAttribArray( 1 );
            GLES30.glEnableVertexAttribArray( 2 );

            // Bind to the index VBO that has all the information about the order of the vertices
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, mVbo.get(1) );

            // Draw everything
            GLES30.glDrawElementsInstanced( GLES11.GL_TRIANGLES, ibo_arr.length, GLES20.GL_UNSIGNED_BYTE, 0, vbo_offset_arr.length/6 );

            // Put everything back to default (deselect)
            GLES30.glBindBuffer( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );
            GLES30.glDisableVertexAttribArray( 0 );
            GLES30.glDisableVertexAttribArray( 1 );
            GLES30.glDisableVertexAttribArray( 2 );
            GLES30.glBindVertexArray( 0 );
        }
    }

    private static final float[][] rainbow_colors = {
            {148f/255f,     0,          211f/255f},
            {75f/255f,      0,          130f/255f},
            {0,             0,          255f/255f},
            {0,             255f/255f,  0},
            {255f/255f,     255f/255f,  0},
            {255f/255f,     127f/255f,  0},
            {255f/255f,     0,          0}
    };

    private static float[] getRainbow(float degree) {
        if( degree > 1 ) {
            degree = 1;
        }else if( degree < 0 ){
            degree = 0;
        }
        // Get two closest colors
        int first = (int) Math.floor(degree * (rainbow_colors.length-1));
        int second = (int) Math.ceil(degree * (rainbow_colors.length-1));

        // Blend both colors
        float[] result = { 0, 0, 0, 0 };
        for( int i=0 ; i<3 ; i++ ) {
            result[i] = (float) Math.sqrt(Math.pow(rainbow_colors[first][i], 2) + Math.pow(rainbow_colors[second][i], 2));
//            result[i] = (float) (rainbow_colors[first][i] + rainbow_colors[second][i])/2f;
        }

        // Set alpha channel
        result[3] = 1;

        return result;
    };

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
        GLES30.glBindAttribLocation( mProgram, 2, "in_offset" );

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
            float dead_zone = 0.7f;
            scale = (float) (dead_zone + (1-dead_zone)*Math.abs(Math.sin(running_bpm_count)));
            rotation = (float) (13.0*Math.sin(running_bpm_count));
        }

        // Transform the scene
        Matrix.setIdentityM(mMMatrix, 0);
        Matrix.translateM(mMMatrix, 0, shake_dx, shake_dy, 0);
//        Matrix.rotateM(mMMatrix, 0, rotation, 0, 1, 0);
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, mMMatrix, 0);

        float sections = MainActivity.BUCKETS;
        float scene_width = 10;
        float scene_height = 10;
        float left = -scene_width/2;
        float right = scene_width/2;
        float top = -scene_height/2;
        float bottom = scene_height/2;
        float step_x = scene_width/sections;
        float step_y = scene_height/sections;
        float tile_size = scene_width / sections;

        // Draw objects
        float flash = (float) (Math.min(running_bpm_count, 2.0f*Math.PI) / 50.0f*Math.PI);
        gl.glClearColor(flash, flash, flash, 1);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);



        // Create instance array
        float[] instance_vbo = new float[(int) (6*(sections+1)*(sections+1))];
        float[] set_color = new float[4];
        double[] frequency_amplitudes = new double[(int) sections+1];
        int max_count = 100;
        // Get frequency graph
        if( mDataRef.data() != null ) {
            frequency_amplitudes = mDataRef.data().freq_counts;
        }
        float darkness_constant = 0.4f;
        int i=0, j=0;
        for( float ty=top, py=0 ; ty<=bottom ; ty+=step_y ) {
            set_color = getRainbow( py );
            py += 1/sections;
            for( float tx=left, px=0 ; tx<=right ; tx+=step_x ) {
                px += 1/sections;
                instance_vbo[i++] = tx;
                instance_vbo[i++] = ty;
                instance_vbo[i++] = tile_size * scale;
                instance_vbo[i++] = set_color[0];
                instance_vbo[i++] = set_color[1];
                instance_vbo[i++] = set_color[2];

                if( px > frequency_amplitudes[j] ){
                    instance_vbo[i-1] = Math.max(instance_vbo[i-1]*darkness_constant, 0);
                    instance_vbo[i-2] = Math.max(instance_vbo[i-2]*darkness_constant, 0);
                    instance_vbo[i-3] = Math.max(instance_vbo[i-3]*darkness_constant, 0);
                }
            }
            j++;
        }
        mCube.draw(gl, instance_vbo);

        // Check for errors
        int err = gl.glGetError();
        if( err != GLES10.GL_NO_ERROR ) {
            Log.w("OpenGL Error", "Error Code:"+err);
        }

        gl.glFinish();
    }
}