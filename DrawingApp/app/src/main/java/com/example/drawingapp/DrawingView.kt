package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

enum class PenShape { ROUND, SQUARE, STAR }

class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class PathData(
        val path: Path,
        val paint: Paint,
        val shape: PenShape
    )

    private val paths = mutableListOf<PathData>()
    private var currentPath = Path()
    private var currentPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var currentShape = PenShape.ROUND

    private var startX = 0f
    private var startY = 0f

    fun setPaintColor(color: Int) {
        currentPaint.color = color
    }

    fun setStrokeWidth(width: Float) {
        currentPaint.strokeWidth = width
    }

    fun setAlpha(alpha: Int) {
        currentPaint.alpha = alpha
    }

    fun setShape(shape: PenShape) {
        currentShape = shape
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (pathData in paths) {
            when (pathData.shape) {
                PenShape.SQUARE -> {
                    val bounds = RectF()
                    pathData.path.computeBounds(bounds, true)
                    drawSquare(canvas, bounds, pathData.paint)
                }
                PenShape.STAR -> drawStar(canvas, pathData.path, pathData.paint)
                else -> canvas.drawPath(pathData.path, pathData.paint)
            }
        }
        canvas.drawPath(currentPath, currentPaint)
    }

    private fun drawSquare(canvas: Canvas, bounds: RectF, paint: Paint) {
        canvas.drawRect(bounds, paint)
    }

    private fun drawStar(canvas: Canvas, path: Path, paint: Paint) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                startX = x
                startY = y
                when (currentShape) {
                    PenShape.STAR -> drawStar(x, y)
                    PenShape.SQUARE -> {
                        // No action needed at this point for SQUARE
                    }
                    else -> currentPath.moveTo(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentShape) {
                    PenShape.SQUARE -> {
                        currentPath.reset()
                        val left = minOf(startX, event.x)
                        val top = minOf(startY, event.y)
                        val right = maxOf(startX, event.x)
                        val bottom = maxOf(startY, event.y)
                        currentPath.addRect(left, top, right, bottom, Path.Direction.CW)
                    }
                    else -> currentPath.lineTo(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP -> {
                when (currentShape) {
                    PenShape.SQUARE -> {
                        val left = minOf(startX, event.x)
                        val top = minOf(startY, event.y)
                        val right = maxOf(startX, event.x)
                        val bottom = maxOf(startY, event.y)
                        currentPath.reset()
                        currentPath.addRect(left, top, right, bottom, Path.Direction.CW)
                    }
                    else -> currentPath.lineTo(event.x, event.y)
                }
                paths.add(PathData(Path(currentPath), Paint(currentPaint), currentShape))
                currentPath.reset()
            }
        }

        invalidate()
        return true
    }

    private fun drawStar(x: Float, y: Float) {
        val outerRadius = currentPaint.strokeWidth * 5f
        val innerRadius = outerRadius / 2.5f
        val path = Path()

        for (i in 0 until 10) {
            val angle = (i * 36.0) * Math.PI / 180.0
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val pointX = x + (radius * sin(angle)).toFloat()
            val pointY = y - (radius * cos(angle)).toFloat()
            if (i == 0) path.moveTo(pointX, pointY) else path.lineTo(pointX, pointY)
        }

        path.close()
        currentPath.addPath(path)
    }

    fun clearCanvas() {
        paths.clear()
        invalidate()
    }

    fun getPaths(): List<PathData> {
        return paths
    }

    fun loadPaths(loadedPaths: List<PathData>) {
        paths.clear()
        paths.addAll(loadedPaths)
        invalidate()
    }
}
