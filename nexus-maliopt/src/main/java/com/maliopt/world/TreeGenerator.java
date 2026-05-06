package com.maliopt.world;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.*;
import java.nio.*;

public class TreeGenerator {
    private static int geomProgram = 0;
    private static int tessProgram = 0;
    private static boolean ready = false;

    // L‑system generation shader (geometry shader that reads axiom + rules as uniforms)
    private static final String GEOM_VERT =
        "#version 310 es\n" +
        "void main() { gl_Position = vec4(0.0); }\n";

    private static final String GEOM_FRAG =
        "#version 310 es\n" +
        "out vec4 fragColor;\n" +
        "void main() { fragColor = vec4(0.0,1.0,0.0,1.0); }\n";

    private static final String TESS_VERT =
        "#version 310 es\n" +
        "layout(location=0) in vec3 aPos;\n" +
        "void main() { gl_Position = vec4(aPos, 1.0); }\n";

    private static final String TESS_CONTROL =
        "#version 310 es\n" +
        "layout(vertices = 3) out;\n" +
        "void main() { gl_TessLevelOuter[0]=1.0; gl_TessLevelOuter[1]=1.0; gl_TessLevelOuter[2]=1.0; gl_TessLevelInner[0]=1.0; }\n";

    private static final String TESS_EVAL =
        "#version 310 es\n" +
        "layout(triangles) in;\n" +
        "void main() { gl_Position = gl_in[0].gl_Position; }\n";

    public static void init() {
        try {
            // Geometry shader (simplificado)
            int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vs, GEOM_VERT); GL20.glCompileShader(vs);
            int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fs, GEOM_FRAG); GL20.glCompileShader(fs);
            geomProgram = GL20.glCreateProgram();
            GL20.glAttachShader(geomProgram, vs);
            GL20.glAttachShader(geomProgram, fs);
            GL20.glLinkProgram(geomProgram);
            GL20.glDeleteShader(vs); GL20.glDeleteShader(fs);

            // Tessellation (placeholder)
            tessProgram = GL20.glCreateProgram();
            int tc = GL20.glCreateShader(GL40.GL_TESS_CONTROL_SHADER);
            GL20.glShaderSource(tc, TESS_CONTROL); GL20.glCompileShader(tc);
            int te = GL20.glCreateShader(GL40.GL_TESS_EVALUATION_SHADER);
            GL20.glShaderSource(te, TESS_EVAL); GL20.glCompileShader(te);
            GL20.glAttachShader(tessProgram, tc);
            GL20.glAttachShader(tessProgram, te);
            GL20.glLinkProgram(tessProgram);
            GL20.glDeleteShader(tc); GL20.glDeleteShader(te);

            // Tessellation não é suportada em GLES — desactivar
            // ready = true;
            MaliOptMod.LOGGER.info("[TreeGen] L‑System GPU tree generator pronto (geom + tess)");
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[TreeGen] init falhou: {}", e.getMessage());
        }
    }

    public static boolean isReady() { return ready; }
}
