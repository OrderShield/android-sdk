package com.ordershieldsdk.auth.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var faces = listOf<Face>()
    private var imageWidth = 0
    private var imageHeight = 0
    private var transformationMatrix = Matrix()

    /**
     * Updates the face list and calculates the transformation matrix
     * based on the camera image dimensions vs view dimensions.
     */
    fun setFaces(faceList: List<Face>, width: Int, height: Int) {
        this.faces = faceList
        this.imageWidth = width
        this.imageHeight = height

        calculateTransformation()
        invalidate()
    }

    private fun calculateTransformation() {
        transformationMatrix.reset()

        // 1. Scale to fit the view
        val scaleX = width.toFloat() / imageWidth.toFloat()
        val scaleY = height.toFloat() / imageHeight.toFloat()
        transformationMatrix.postScale(scaleX, scaleY)

        // 2. Mirror horizontally (for front camera)
        transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (face in faces) {
            val boundingBox = RectF(face.boundingBox)

            // Apply the transformation to the bounding box
            transformationMatrix.mapRect(boundingBox)

            // Draw a rounded rectangle or circle around the face
            // Using a circle/oval looks cleaner for selfies
            canvas.drawOval(boundingBox, paint)
        }
    }
}