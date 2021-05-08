import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

public class ExampleImGuizmo {

    private ExampleImGuizmo() {
    }

    private static final int CAM_DISTANCE = 8;
    private static final float camYAngle = 165.f / 180.f * 3.14159f;
    private static final float camXAngle = 32.f / 180.f * 3.14159f;
    private static boolean firstFrame = true;

    private static final float FLT_EPSILON = 1.19209290E-07f;

    private static final float[][] objectMatrix = {
        {1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f},

        {1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            2.f, 0.f, 0.f, 1.f},

        {1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            2.f, 0.f, 2.f, 1.f},

        {1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 2.f, 1.f}
    };

    private static final float[] cameraView = {
        1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f};

    private static final float[] identityMatrix = {
        1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f};

    private static float[] perspective(float fovY, float aspect, float near, float far) {
        float ymax, xmax;
        ymax = (float) (near * Math.tan(fovY * 3.141592f / 180.0f));
        xmax = ymax * aspect;
        return frustum(-xmax, xmax, -ymax, ymax, near, far);
    }

    private static float[] frustum(float left, float right, float bottom, float top, float near, float far) {
        float[] matrix16f = new float[16];
        float temp = 2.0f * near;
        float temp2 = right - left;
        float temp3 = top - bottom;
        float temp4 = far - near;
        matrix16f[0] = temp / temp2;
        matrix16f[1] = 0.0f;
        matrix16f[2] = 0.0f;
        matrix16f[3] = 0.0f;
        matrix16f[4] = 0.0f;
        matrix16f[5] = temp / temp3;
        matrix16f[6] = 0.0f;
        matrix16f[7] = 0.0f;
        matrix16f[8] = (right + left) / temp2;
        matrix16f[9] = (top + bottom) / temp3;
        matrix16f[10] = (-far - near) / temp4;
        matrix16f[11] = -1.0f;
        matrix16f[12] = 0.0f;
        matrix16f[13] = 0.0f;
        matrix16f[14] = (-temp * far) / temp4;
        matrix16f[15] = 0.0f;
        return matrix16f;
    }

    private static float[] cross(float[] a, float[] b) {
        float[] r = new float[3];
        r[0] = a[1] * b[2] - a[2] * b[1];
        r[1] = a[2] * b[0] - a[0] * b[2];
        r[2] = a[0] * b[1] - a[1] * b[0];
        return r;
    }


    private static float Dot(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static float[] Normalize(float[] a) {
        float[] r = new float[3];
        float il = (float) (1.f / (Math.sqrt(Dot(a, a)) + FLT_EPSILON));
        r[0] = a[0] * il;
        r[1] = a[1] * il;
        r[2] = a[2] * il;
        return r;
    }

    private static void LookAt(float[] eye, float[] at, float[] up, float[] m16) {
        float[] x;
        float[] y;
        float[] z;
        float[] tmp = new float[3];

        tmp[0] = eye[0] - at[0];
        tmp[1] = eye[1] - at[1];
        tmp[2] = eye[2] - at[2];
        z = Normalize(tmp);
        y = Normalize(up);

        tmp = cross(y, z);
        x = Normalize(tmp);

        tmp = cross(z, x);
        y = Normalize(tmp);

        m16[0] = x[0];
        m16[1] = y[0];
        m16[2] = z[0];
        m16[3] = 0.0f;
        m16[4] = x[1];
        m16[5] = y[1];
        m16[6] = z[1];
        m16[7] = 0.0f;
        m16[8] = x[2];
        m16[9] = y[2];
        m16[10] = z[2];
        m16[11] = 0.0f;
        m16[12] = -Dot(x, eye);
        m16[13] = -Dot(y, eye);
        m16[14] = -Dot(z, eye);
        m16[15] = 1.0f;
    }

    public static void show() {

        ImGuizmo.setOrthographic(false);
        ImGuizmo.beginFrame();
        ImGuizmo.setEnabled(true);

        ImGui.setNextWindowSize(256, 256);
        ImGui.begin("Editor");

        if (firstFrame) {
            float[] eye = {(float) (Math.cos(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE), (float) (Math.sin(camXAngle) * CAM_DISTANCE), (float) (Math.sin(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE)};
            float[] at = {0.f, 0.f, 0.f};
            float[] up = {0.f, 1.f, 0.f};
            LookAt(eye, at, up, cameraView);
            firstFrame = false;
        }

        if (ImGuizmo.isUsing()) ImGui.text("Using gizmo");
        else ImGui.text("Not using gizmo");
        ImGui.text(ImGuizmo.isOver() ? "Over gizmo" : "");
        ImGui.sameLine();
        ImGui.text(ImGuizmo.isOver(Operation.TRANSLATE) ? "Over translate gizmo" : "");
        ImGui.sameLine();
        ImGui.text(ImGuizmo.isOver(Operation.ROTATE) ? "Over rotate gizmo" : "");
        ImGui.sameLine();
        ImGui.text(ImGuizmo.isOver(Operation.SCALE) ? "Over scale gizmo" : "");
        ImGui.separator();

        ImGuizmo.setId(0);
        editTransform();
        ImGui.end();
    }

    private static int currentMode = Mode.LOCAL;
    private static int currentGizmoOperation;
    private static boolean useSnap = false;
    private static final float[] snap = {1f, 1f, 1f};
    private static float[] bounds = {-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
    private static float[] boundsSnap = {0.1f, 0.1f, 0.1f};
    private static boolean boundSizing = false;
    private static boolean boundSizingSnap = false;

    private static void editTransform() {

        if (ImGui.isKeyPressed(90)) currentGizmoOperation = Operation.TRANSLATE;
        if (ImGui.isKeyPressed(69)) currentGizmoOperation = Operation.ROTATE;
        if (ImGui.isKeyPressed(72)) currentGizmoOperation = Operation.SCALE;

        float[] matrixTranslation = new float[3];
        float[] matrixRotation = new float[3];
        float[] matrixScale = new float[3];

        ImGuizmo.decomposeMatrixToComponents(objectMatrix[0], matrixTranslation, matrixRotation, matrixScale);
        ImGui.inputFloat3("Tr", matrixTranslation);
        ImGui.inputFloat3("Rt", matrixRotation);
        ImGui.inputFloat3("Sc", matrixScale);
        ImGuizmo.recomposeMatrixFromComponents(matrixTranslation, matrixRotation, matrixScale, objectMatrix[0]);
        if (currentGizmoOperation != Operation.SCALE) {
            if (ImGui.radioButton("Local", currentMode == Mode.LOCAL))
                currentMode = Mode.LOCAL;
            ImGui.sameLine();
            if (ImGui.radioButton("World", currentMode == Mode.WORLD))
                currentMode = Mode.WORLD;
        }

        if (ImGui.isKeyPressed(83)) useSnap = !useSnap;
        ImGui.checkbox("Snap Checkbox", useSnap);
        ImGui.sameLine();

        ImFloat snapBuffer = new ImFloat(snap[0]);
        switch (currentGizmoOperation) {
            case Operation.TRANSLATE -> ImGui.inputFloat("Translate Snap", snapBuffer);
            case Operation.ROTATE -> ImGui.inputFloat("Rotate Snap", snapBuffer);
            case Operation.SCALE -> ImGui.inputFloat("Scale Snap", snapBuffer);
        }
        snap[0] = snapBuffer.get();
        snap[1] = snapBuffer.get();
        snap[2] = snapBuffer.get();

        /*if(ImGui.checkbox("Bound Sizing", boundSizing)) boundSizing = !boundSizing;
        if (boundSizing)
        {
            ImGui.pushID(3);
            if(ImGui.checkbox("BoundSizingSnap", boundSizingSnap)) boundSizingSnap = !boundSizingSnap;
            ImGui.sameLine();
            ImGui.inputFloat3("Snap", boundsSnap);
            ImGui.popID();
        }*/

        ImGui.setNextWindowPos(800, 400);
        ImGui.setNextWindowSize(800, 400);
        ImGui.begin("Gizmo");
        ImGuizmo.setDrawList();
        float windowWidth = ImGui.getWindowWidth();
        float windowHeight = ImGui.getWindowHeight();
        ImGuizmo.setRect(ImGui.getWindowPosX(), ImGui.getWindowPosY(), windowWidth, windowHeight);
        float viewManipulateRight = ImGui.getWindowPosX() + windowWidth;
        float viewManipulateTop = ImGui.getWindowPosY();

        float aspect = ImGui.getWindowSizeX() / ImGui.getWindowSizeY();
        float[] cameraProjection = perspective(27, aspect, 0.1f, 100f);
        ImGuizmo.drawGrid(cameraView, cameraProjection, identityMatrix, 100);
        ImGuizmo.drawCubes(cameraView, cameraProjection, objectMatrix[0]);
        if(useSnap)
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode, snap);
        else
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode);

        ImGuizmo.viewManipulate(cameraView, CAM_DISTANCE, new float[]{viewManipulateRight - 128, viewManipulateTop}, new float[]{128, 128}, 0x10101010);

        ImGui.end();
    }

}
