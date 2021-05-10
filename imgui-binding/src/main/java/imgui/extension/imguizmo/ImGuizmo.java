package imgui.extension.imguizmo;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImFloat;

public final class ImGuizmo {

    private static final boolean[] booleanBuffer = new boolean[1];
    private static final float[] floatBuffer = new float[3]; //for snap

     /*JNI
       #include <imgui.h>
       #include <ImGuizmo.h>
       #include "jni_common.h"
    */

    private static native void nEnabled(boolean buffer); /*
        ImGuizmo::Enable(&buffer);
    */

    /**
     * Enable/Disable the gizmo.
     */
    public static void setEnabled(boolean isEnabled) {
        nEnabled(isEnabled);
    }

    private static native void nIsUsing(boolean[] buffer); /*
        buffer[0] = ImGuizmo::IsUsing();
    */

    /**
     * Checks to see if we're using the Gizmo
     */
    public static boolean isUsing() {
        nIsUsing(booleanBuffer);
        return booleanBuffer[0];
    }

    private static native void nIsOver(boolean[] buffer); /*
        buffer[0] = ImGuizmo::IsOver();
    */

    /**
     * Checks to see if we're over the Gizmo
     */
    public static boolean isOver() {
        nIsOver(booleanBuffer);
        return booleanBuffer[0];
    }

    private static native void nSetDrawList(long pointer); /*
        ImGuizmo::SetDrawlist((ImDrawList*)pointer);
    */

    /**
     * Setting the draw list of the given Gizmo
     */
    public static void setDrawList(ImDrawList drawList) {
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

    private static native void nDecomposeMatrixToComponents(float[] matrix, float[] translation, float[] rotation, float[] scale); /*
        ImGuizmo::DecomposeMatrixToComponents(matrix, translation, rotation, scale);
    */

    /**
     * Decomposing or disassembling the matrix to its components
     * Call this before recomposing.
     */
    public static void decomposeMatrixToComponents(float[] matrix, float[] translation, float[] rotation, float[] scale) {
        nDecomposeMatrixToComponents(matrix, translation, rotation, scale);
    }

    private static native void nRecomposeMatrixFromComponents(float[] translation, float[] rotation, float[] scale, float[] matrix); /*
        ImGuizmo::RecomposeMatrixFromComponents(translation, rotation, scale, matrix);
    */

    /**
     * Recompose the given matrix to its original form.
     * Call this after decomposing.
     */
    public static void recomposeMatrixFromComponents(float[] matrix, float[] translation, float[] rotation, float[] scale) {
        nRecomposeMatrixFromComponents(translation, rotation, scale, matrix);
    }

    public static native void nSetRect(float x, float y, float width, float height); /*
        ImGuizmo::SetRect(x, y, width, height);
    */

    /**
     * This will set the rect position
     */
    public static void setRect(float x, float y, float width, float height) {
        nSetRect(x, y, width, height);
    }


    /**
     * This will set the rect position to the default current rect position
     */
    public static void setRect() {
        ImVec2 pos = ImGui.getWindowPos();
        ImVec2 size = ImGui.getWindowSize();
        setRect(pos.x, pos.y, size.x, size.y);
    }


    private static native void nSetOrthographic(boolean ortho); /*
        ImGuizmo::SetOrthographic(ortho);
    */

    /**
     * Making sure if we're set to ortho or not
     */
    public static void setOrthographic(boolean ortho) {
        nSetOrthographic(ortho);
    }

    private static native void nDrawCubes(float[] view, float[] projection, float[] matrices, int matrixCount); /*
        ImGuizmo::DrawCubes(view, projection, matrices, matrixCount);
    */

    /**
     * Drawing an arbitrary cube in the world.
     * Mainly for debugging purposes
     */
    public static void drawCubes(float[] view, float[] projection, float[]... cubeMatrices) {
        float[] matrices = new float[cubeMatrices.length * 16];
        int index = 0;
        for (int i = 0; i < cubeMatrices.length; i++) {
            for (int j = 0; j < 16; j++) {
                matrices[index++] = cubeMatrices[i][j];
            }
        }
        nDrawCubes(view, projection, matrices, cubeMatrices.length);
    }

    private static native void nDrawGrid(float[] view, float[] projection, float[] matrix, int gridSize); /*
        ImGuizmo::DrawGrid(view, projection, matrix, gridSize);
    */

    public static void drawGrid(float[] view, float[] projection, float[] matrix, int gridSize) {
        nDrawGrid(view, projection, matrix, gridSize);
    }

    private native static void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix);/*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix);
    */

    private native static void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] snap);/*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap);
    */

    private native static void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, final float[] snap, final float[] bounds);/*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap, bounds, NULL);
    */

    private native static void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, final float[] snap, final float[] bounds, final float[] boundsSnap);/*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, NULL, snap, bounds, boundsSnap);
    */

    private native static void nManipulate(float[] view, float[] projection, int operation, int mode, float[] matrix, float[] deltaMatrix, final float[] snap, final float[] bounds, final float[] boundsSnap);/*
        ImGuizmo::Manipulate(view, projection, (ImGuizmo::OPERATION) operation, (ImGuizmo::MODE) mode, matrix, deltaMatrix, snap, bounds, boundsSnap);
    */

    /**
     * Manipulating the given object matrix
     */
    public static void manipulate(float[] view, float[] projection, float[] modelMatrix, int operation, int mode) {
        nManipulate(view, projection, operation, mode, modelMatrix);
    }

    /**
     * Manipulating the given object matrix with snap feature enabled!
     */
    public static void manipulate(float[] view, float[] projection, float[] modelMatrix, int operation, int mode, float[] snap){
        nManipulate(view, projection, operation, mode, modelMatrix, snap);
    }

    /**
     * Manipulating the given object matrix with snap and bounds feature enabled!
     */
    public static void manipulate(float[] view, float[] projection, float[] modelMatrix, int operation, int mode, float[] snap, float[] bounds){
        nManipulate(view, projection, operation, mode, modelMatrix, snap, bounds);
    }

    /**
     * Manipulating the given object matrix with snap and bounds(snap) feature enabled!
     */
    public static void manipulate(float[] view, float[] projection, float[] modelMatrix, int operation, int mode, float[] snap, float[] bounds, float[] boundsSnap){
        nManipulate(view, projection, operation, mode, modelMatrix, snap, bounds, boundsSnap);
    }

    /**
     * Manipulating the given object matrix
     */
    public static void manipulate(float[] view, float[] projection, float[] modelMatrix, float[] deltaMatrix, int operation, int mode, float[] snap, float[] bounds, float[] boundsSnap){
        nManipulate(view, projection, operation, mode, modelMatrix, deltaMatrix, snap, bounds, boundsSnap);
    }

    private static native void nViewManipulate(float[] view, float length, float[] position, float[] size, int color);/*
        ImGuizmo::ViewManipulate(view, length, ImVec2(position[0], position[1]), ImVec2(size[0], size[1]), (ImU32) color);
    */

    /**
     * This will do the view manipulation
     */
    public static void viewManipulate(float[] view, float length, float[] position, float[] size, int color) {
        nViewManipulate(view, length, position, size, color);
    }

    private static native void nSetId(int id);/*
        ImGuizmo::SetID(id);
    */

    /**
     * This will update the current id
     */
    public static void setId(int id) {
        nSetId(id);
    }

    private static native boolean nIsOver(int operation);/*
        return ImGuizmo::IsOver((ImGuizmo::OPERATION) operation);
    */

    /**
     * Checks if we're over the current operation
     */
    public static boolean isOver(int operation) {
        return nIsOver(operation);
    }

    private static native void nSetGizmoSizeClipSpace(float value);/*
        ImGuizmo::SetGizmoSizeClipSpace(value);
    */

    private static native void nAllowAxisFlip(boolean value);/*
        ImGuizmo::AllowAxisFlip(value);
     */

    /**
     * This will update the current axis flip value
     */
    public static void setAllowAxisFlip(boolean value) {
        nAllowAxisFlip(value);
    }
}
