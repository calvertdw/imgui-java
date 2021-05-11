package imgui.extension.imguizmo;

import imgui.ImDrawList;
import imgui.ImGui;

public final class ImGuizmo {

    private static float[] matrices = null; //for drawCubes()

    private ImGuizmo() {
    }

     /*JNI
       #include "_common.h"
       #include "ImGuizmo.h"
     */

    private static native void nEnabled(boolean enabled); /*
        ImGuizmo::Enable(enabled);
    */

    /**
     * Enable/Disable the gizmo.
     */
    public static void setEnabled(final boolean isEnabled) {
        nEnabled(isEnabled);
    }

    private static native boolean nIsUsing(); /*
        return (jboolean) ImGuizmo::IsUsing();
    */

    /**
     * Checks to see if we're using the Gizmo
     */
    public static boolean isUsing() {
        return nIsUsing();
    }

    private static native boolean nIsOver(); /*
        return (jboolean) ImGuizmo::IsOver();
    */

    /**
     * Checks to see if we're over the Gizmo
     */
    public static boolean isOver() {
        return nIsOver();
    }

    private static native void nSetDrawList(long pointer); /*
        ImGuizmo::SetDrawlist((ImDrawList*)pointer);
    */

    /**
     * Setting the draw list of the given Gizmo
     */
    public static void setDrawList(final ImDrawList drawList) {
        nSetDrawList(drawList.ptr);
    }


    /**
     * Setting the default window drawlist
     */
    public static void setDrawList() {
        setDrawList(ImGui.getWindowDrawList());
    }

    /**
     * Starts the next frame for the Gizmo
     * Call this after you've called ImGui.beginFrame()
     */
    public static native void beginFrame(); /*
        ImGuizmo::BeginFrame();
    */

    private static native void nDecomposeMatrixToComponents(final float[] matrix, final float[] translation, final float[] rotation, final float[] scale); /*
        ImGuizmo::DecomposeMatrixToComponents(matrix, translation, rotation, scale);
    */

    /*
     * helper functions for manualy editing translation/rotation/scale with an input float
     * translation, rotation and scale float points to 3 floats each
     * Angles are in degrees (more suitable for human editing)
     * example:
     * float matrixTranslation[3], matrixRotation[3], matrixScale[3];
     * ImGuizmo::DecomposeMatrixToComponents(gizmoMatrix.m16, matrixTranslation, matrixRotation, matrixScale);
     * ImGui::InputFloat3("Tr", matrixTranslation, 3);
     * ImGui::InputFloat3("Rt", matrixRotation, 3);
     * ImGui::InputFloat3("Sc", matrixScale, 3);
     * ImGuizmo::RecomposeMatrixFromComponents(matrixTranslation, matrixRotation, matrixScale, gizmoMatrix.m16);
     * These functions have some numerical stability issues for now. Use with caution.
     */
    public static void decomposeMatrixToComponents(final float[] matrix, final float[] translation, final float[] rotation, final float[] scale) {
        nDecomposeMatrixToComponents(matrix, translation, rotation, scale);
    }

    private static native void nRecomposeMatrixFromComponents(float[] matrix, float[] translation, float[] rotation, float[] scale); /*
        ImGuizmo::RecomposeMatrixFromComponents(translation, rotation, scale, matrix);
    */

    /*
     * helper functions for manualy editing translation/rotation/scale with an input float
     * translation, rotation and scale float points to 3 floats each
     * Angles are in degrees (more suitable for human editing)
     * example:
     * float matrixTranslation[3], matrixRotation[3], matrixScale[3];
     * ImGuizmo::DecomposeMatrixToComponents(gizmoMatrix.m16, matrixTranslation, matrixRotation, matrixScale);
     * ImGui::InputFloat3("Tr", matrixTranslation, 3);
     * ImGui::InputFloat3("Rt", matrixRotation, 3);
     * ImGui::InputFloat3("Sc", matrixScale, 3);
     * ImGuizmo::RecomposeMatrixFromComponents(matrixTranslation, matrixRotation, matrixScale, gizmoMatrix.m16);
     * These functions have some numerical stability issues for now. Use with caution.
     */
    public static void recomposeMatrixFromComponents(final float[] matrix, final float[] translation, final float[] rotation, final float[] scale) {
        nRecomposeMatrixFromComponents(matrix, translation, rotation, scale);
    }

    public static native void nSetRect(float x, float y, float width, float height); /*
        ImGuizmo::SetRect(x, y, width, height);
    */

    /**
     * This will set the rect position
     */
    public static void setRect(final float x, final float y, final float width, final float height) {
        nSetRect(x, y, width, height);
    }


    private static native void nSetOrthographic(boolean ortho); /*
        ImGuizmo::SetOrthographic(ortho);
    */

    /**
     * Making sure if we're set to ortho or not
     */
    public static void setOrthographic(final boolean ortho) {
        nSetOrthographic(ortho);
    }

    private static native void nDrawCubes(float[] view, float[] projection, float[] matrices, int matrixCount); /*
        ImGuizmo::DrawCubes(view, projection, matrices, matrixCount);
    */

    /**
     * Drawing an arbitrary cube in the world.
     * NOTE: Supports up to 4 cubes max. since this method should only be used for debugging purposes
     */
    public static void drawCubes(final float[] view, final float[] projection, final float[]... cubeMatrices) {
        if (cubeMatrices.length > 4) {
            System.err.println("Drawing cubes with ImGuizmo only supports up to 4 cubes because it should only be used for debugging purposes");
            return;
        }
        if (matrices == null) {
            matrices = new float[4 * 16]; //allocating enough, if someone wants to render cubes for debugging
        }
        int index = 0;
        for (float[] cubeMatrix : cubeMatrices) {
            System.arraycopy(cubeMatrix, 0, matrices, index++ * cubeMatrix.length, cubeMatrix.length); //copying like that does perform better than regular for loops
        }
        nDrawCubes(view, projection, matrices, cubeMatrices.length);
    }

    private static native void nDrawGrid(float[] view, float[] projection, float[] matrix, int gridSize); /*
        ImGuizmo::DrawGrid(view, projection, matrix, gridSize);
    */

    public static void drawGrid(final float[] view, final float[] projection, final float[] matrix, final int gridSize) {
        nDrawGrid(view, projection, matrix, gridSize);
    }

    private static native void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix); /*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix);
    */

    private static native void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] snap); /*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap);
    */

    private static native void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] snap, float[] bounds); /*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap, bounds, NULL);
    */

    private static native void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] snap, float[] bounds, float[] boundsSnap); /*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap, bounds, boundsSnap);
    */

    private static native void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] deltaMatrix, float[] snap, float[] bounds, float[] boundsSnap); /*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, deltaMatrix, snap, bounds, boundsSnap);
    */

    /**
     * Manipulating the given object matrix
     */
    public static void manipulate(final float[] view, final float[] projection, final float[] modelMatrix, final int operation, final int mode) {
        nManipulate(view, projection, operation, mode, modelMatrix);
    }

    /**
     * Manipulating the given object matrix with snap feature enabled!
     */
    public static void manipulate(final float[] view, final float[] projection, final float[] modelMatrix, final int operation, final int mode, final float[] snap) {
        nManipulate(view, projection, operation, mode, modelMatrix, snap);
    }

    /**
     * Manipulating the given object matrix with snap and bounds feature enabled!
     */
    public static void manipulate(final float[] view, final float[] projection, final float[] modelMatrix, final int operation, final int mode, final float[] snap, final float[] bounds) {
        nManipulate(view, projection, operation, mode, modelMatrix, snap, bounds);
    }

    /**
     * Manipulating the given object matrix with snap and bounds(snap) feature enabled!
     */
    public static void manipulate(final float[] view, final float[] projection, final float[] modelMatrix, final int operation, final int mode, final float[] snap, final float[] bounds, final float[] boundsSnap) {
        nManipulate(view, projection, operation, mode, modelMatrix, snap, bounds, boundsSnap);
    }

    /**
     * Manipulating the given object matrix
     */
    public static void manipulate(final float[] view, final float[] projection, final float[] modelMatrix, final float[] deltaMatrix, final int operation, final int mode, final float[] snap, final float[] bounds, final float[] boundsSnap) {
        nManipulate(view, projection, operation, mode, modelMatrix, deltaMatrix, snap, bounds, boundsSnap);
    }

    private static native void nViewManipulate(float[] view, float length, float[] position, float[] size, int color); /*
        ImGuizmo::ViewManipulate(view, length, ImVec2(position[0], position[1]), ImVec2(size[0], size[1]), (ImU32) color);
    */

    /**
     * This will do the view manipulation
     */
    public static void viewManipulate(final float[] view, final float length, final float[] position, final float[] size, final int color) {
        nViewManipulate(view, length, position, size, color);
    }

    private static native void nSetId(int id); /*
        ImGuizmo::SetID(id);
    */

    /**
     * This will update the current id
     */
    public static void setId(final int id) {
        nSetId(id);
    }

    private static native boolean nIsOver(int operation); /*
        return ImGuizmo::IsOver((ImGuizmo::OPERATION) operation);
    */

    /**
     * Checks if we're over the current operation
     */
    public static boolean isOver(final int operation) {
        return nIsOver(operation);
    }

    private static native void nSetGizmoSizeClipSpace(float value); /*
        ImGuizmo::SetGizmoSizeClipSpace(value);
    */

    private static native void nAllowAxisFlip(boolean value); /*
        ImGuizmo::AllowAxisFlip(value);
     */

    /**
     * This will update the current axis flip value
     */
    public static void setAllowAxisFlip(final boolean value) {
        nAllowAxisFlip(value);
    }
}
