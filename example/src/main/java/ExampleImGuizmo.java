import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.type.ImFloat;

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
        float[] r = new float[16];
        float temp = 2.0f * near;
        float temp2 = right - left;
        float temp3 = top - bottom;
        float temp4 = far - near;
        r[0] = temp / temp2;
        r[1] = 0.0f;
        r[2] = 0.0f;
        r[3] = 0.0f;
        r[4] = 0.0f;
        r[5] = temp / temp3;
        r[6] = 0.0f;
        r[7] = 0.0f;
        r[8] = (right + left) / temp2;
        r[9] = (top + bottom) / temp3;
        r[10] = (-far - near) / temp4;
        r[11] = -1.0f;
        r[12] = 0.0f;
        r[13] = 0.0f;
        r[14] = (-temp * far) / temp4;
        r[15] = 0.0f;
        return r;
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

        ImGui.setNextWindowSize(256, 256);
        ImGui.begin("Editor");

        if (firstFrame) {
            float[] eye = {(float) (Math.cos(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE), (float) (Math.sin(camXAngle) * CAM_DISTANCE), (float) (Math.sin(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE)};
            float[] at = {0.f, 0.f, 0.f};
            float[] up = {0.f, 1.f, 0.f};
            LookAt(eye, at, up, cameraView);
            firstFrame = false;
        }

        ImGuizmo.beginFrame();
        if (ImGuizmo.isUsing()) {
            ImGui.text("Using gizmo");
            ImGui.text(ImGuizmo.isOver() ? "Over gizmo" : "");
            ImGui.text(ImGuizmo.isOver(Operation.TRANSLATE) ? "Over translate gizmo" : "");
            ImGui.text(ImGuizmo.isOver(Operation.ROTATE) ? "Over rotate gizmo" : "");
            ImGui.text(ImGuizmo.isOver(Operation.SCALE) ? "Over scale gizmo" : "");
        } else ImGui.text("Not using gizmo");
        ImGui.separator();

        editTransform();
        ImGui.end();
    }

    private static int currentMode = Mode.LOCAL;
    private static int currentGizmoOperation;

    private static boolean useSnap = false;

    private static final float[] bounds = {-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
    private static final float[] boundsSnap = {0.1f, 0.1f, 0.1f};
    private static boolean boundSizing = false;
    private static boolean boundSizingSnap = false;

    private static final float[] viewManipulateSize = {128f, 128f};
    private static float[] snapValue = {0.5f, 0.5f, 0.5f};

    private static void editTransform() {

        if (ImGui.isKeyPressed(90)) currentGizmoOperation = Operation.TRANSLATE;
        if (ImGui.isKeyPressed(69)) currentGizmoOperation = Operation.ROTATE;
        if (ImGui.isKeyPressed(72)) currentGizmoOperation = Operation.SCALE;
        if (ImGui.isKeyPressed(83)) useSnap = !useSnap;

        final float[] matrixTranslation = new float[3];
        final float[] matrixRotation = new float[3];
        final float[] matrixScale = new float[3];

        ImGuizmo.recomposeMatrixFromComponents(matrixTranslation, matrixRotation, matrixScale, objectMatrix[0]);
        ImGui.inputFloat3("Tr", matrixTranslation);
        ImGui.inputFloat3("Rt", matrixRotation);
        ImGui.inputFloat3("Sc", matrixScale);
        if (currentGizmoOperation != Operation.SCALE) {
            if (ImGui.radioButton("Local", currentMode == Mode.LOCAL))
                currentMode = Mode.LOCAL;
            ImGui.sameLine();
            if (ImGui.radioButton("World", currentMode == Mode.WORLD))
                currentMode = Mode.WORLD;
        }

        if (ImGui.checkbox("Snap Checkbox", useSnap)) useSnap = !useSnap;
        ImFloat imFloatBuffer = new ImFloat(snapValue[0]);
        switch (currentGizmoOperation) {
            case Operation.TRANSLATE -> ImGui.inputFloat3("Snap Value", snapValue);
            case Operation.ROTATE -> {
                ImGui.inputFloat("Angle Value", imFloatBuffer);
                snapValue = imFloatBuffer.getData();
            }
            case Operation.SCALE -> {
                ImGui.inputFloat("Scale Value", imFloatBuffer);
                snapValue = imFloatBuffer.getData();
            }
        }

        if (ImGui.checkbox("Bound Sizing", boundSizing)) boundSizing = !boundSizing;
        if (boundSizing) {
            ImGui.pushID(3);
            if (ImGui.checkbox("BoundSizingSnap", boundSizingSnap)) boundSizingSnap = !boundSizingSnap;
            ImGui.sameLine();
            ImGui.inputFloat3("Snap", boundsSnap);
            ImGui.popID();
        }

        ImGui.setNextWindowPos(800, 400);
        ImGui.setNextWindowSize(800, 400);
        ImGui.begin("Gizmo");

        ImGuizmo.setOrthographic(false);
        ImGuizmo.setEnabled(true);
        ImGuizmo.setDrawList();

        float windowWidth = ImGui.getWindowWidth();
        float windowHeight = ImGui.getWindowHeight();
        ImGuizmo.setRect(ImGui.getWindowPosX(), ImGui.getWindowPosY(), windowWidth, windowHeight);

        float aspect = ImGui.getWindowSizeX() / ImGui.getWindowSizeY();
        float[] cameraProjection = perspective(27, aspect, 0.1f, 100f);

        ImGuizmo.setId(0);
        ImGuizmo.drawGrid(cameraView, cameraProjection, identityMatrix, 100);
        ImGuizmo.drawCubes(cameraView, cameraProjection, objectMatrix[0]);

        //So you might be cussing right now and saying why didn't you do this:
        //ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode, useSnap ? snapValue : null, boundSizing ? bounds : null,...);
        //Yes that way, it would've been better code-wise but when i give null parameter to it, it just crashes...
        if (useSnap && boundSizing && boundSizingSnap)
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode, snapValue, bounds, boundsSnap);
        else if(useSnap && boundSizing)
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode, snapValue, bounds);
        else if(useSnap)
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode, snapValue);
        else
            ImGuizmo.manipulate(cameraView, cameraProjection, objectMatrix[0], currentGizmoOperation, currentMode);

        float viewManipulateRight = ImGui.getWindowPosX() + windowWidth;
        float viewManipulateTop = ImGui.getWindowPosY();
        ImGuizmo.viewManipulate(cameraView, CAM_DISTANCE, new float[]{viewManipulateRight - 128, viewManipulateTop}, viewManipulateSize, 0x10101010);

        ImGui.end();
    }

}
