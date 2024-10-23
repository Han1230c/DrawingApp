package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
enum class PenShape { ROUND, SQUARE, STAR, BALL }
class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

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
    var currentShape = PenShape.ROUND

    private var startX = 0f
    private var startY = 0f

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null

    private var isBallMode = false
    private var ballX = 0f
    private var ballY = 0f
    private var ballRadius = 30f
    private var lastBallX = 0f
    private var lastBallY = 0f

    private val ballPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val ballPathPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var ballPath = Path()

    private var velocityX = 0f
    private var velocityY = 0f
    private val damping = 0.98f
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val shakeThreshold = 100
    private var baseStrokeWidth = 8f
    private var maxStrokeWidth = 50f

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun getCurrentPaint(): Paint = Paint(currentPaint)

    fun setPaintColor(color: Int) {
        currentPaint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        currentPaint.strokeWidth = width.coerceIn(baseStrokeWidth, maxStrokeWidth)
        strokeWidthChangedListener?.invoke(currentPaint.strokeWidth)
    }

    private var strokeWidthChangedListener: ((Float) -> Unit)? = null

    fun setOnStrokeWidthChangedListener(listener: (Float) -> Unit) {
        strokeWidthChangedListener = listener
    }

    fun setAlpha(alpha: Int) {
        currentPaint.alpha = alpha
        invalidate()
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

        if (isBallMode) {
            canvas.drawPath(ballPath, ballPathPaint)
            canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
        } else {
            canvas.drawPath(currentPath, currentPaint)
        }
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
                    PenShape.STAR -> {
                        // Do nothing yet, wait until ACTION_UP to draw star
                    }
                    PenShape.SQUARE -> {
                        // Do nothing yet for square
                    }
                    else -> currentPath.moveTo(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentShape) {
                    PenShape.SQUARE -> {
                        currentPath.reset()
                        val left = min(startX, event.x)
                        val top = min(startY, event.y)
                        val right = max(startX, event.x)
                        val bottom = max(startY, event.y)
                        currentPath.addRect(left, top, right, bottom, Path.Direction.CW)
                    }
                    else -> currentPath.lineTo(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP -> {
                when (currentShape) {
                    PenShape.STAR -> {
                        drawStar(startX, startY) // Draw star only at ACTION_UP
                    }
                    PenShape.SQUARE -> {
                        val left = min(startX, event.x)
                        val top = min(startY, event.y)
                        val right = max(startX, event.x)
                        val bottom = max(startY, event.y)
                        currentPath.reset()
                        currentPath.addRect(left, top, right, bottom, Path.Direction.CW)
                    }
                    else -> currentPath.lineTo(event.x, event.y)
                }
                // Add the path to paths list after completing the shape
                paths.add(PathData(Path(currentPath), Paint(currentPaint), currentShape))
                currentPath.reset()
            }
        }

        invalidate() // Refresh the view to show the updated path
        return true
    }

    private fun drawStar(x: Float, y: Float) {
        val outerRadius = currentPaint.strokeWidth * 5f
        val innerRadius = outerRadius / 2.5f
        val angleOffset = Math.PI / 10 // 18 degrees offset for a 5-point star

        currentPath.reset() // Start with a fresh path

        for (i in 0 until 10) {
            val angle = angleOffset + (i * Math.PI / 5)
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val pointX = (x + radius * cos(angle)).toFloat()
            val pointY = (y - radius * sin(angle)).toFloat()
            if (i == 0) {
                currentPath.moveTo(pointX, pointY)
            } else {
                currentPath.lineTo(pointX, pointY)
            }
        }
        currentPath.close()
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

    fun getBitmapFromView(): Bitmap {
        // Create a bitmap with the same dimensions as the view
        val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        // Draw the view's background
        val bgDrawable = background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        // Draw the view onto the canvas
        draw(canvas)
        return returnedBitmap
    }

    fun setBallMode(enabled: Boolean) {
        isBallMode = enabled
        if (enabled) {
            ballX = width / 2f
            ballY = height / 2f
            lastBallX = ballX
            lastBallY = ballY
            velocityX = 0f
            velocityY = 0f
            ballPath.reset()
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            sensorManager.unregisterListener(this)

            if (!ballPath.isEmpty) {
                paths.add(PathData(Path(ballPath), Paint(ballPathPaint), PenShape.BALL))
                ballPath.reset()
            }
        }
        invalidate()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        if (isBallMode) {
            // 现有的球体模式代码保持不变
            val ax = -event.values[0] * 0.8f
            val ay = event.values[1] * 0.8f

            velocityX += ax
            velocityY += ay

            velocityX *= damping
            velocityY *= damping

            lastBallX = ballX
            lastBallY = ballY
            ballX += velocityX
            ballY += velocityY

            if (ballX < ballRadius) {
                ballX = ballRadius
                velocityX = -velocityX * 0.8f
            } else if (ballX > width - ballRadius) {
                ballX = width - ballRadius
                velocityX = -velocityX * 0.8f
            }

            if (ballY < ballRadius) {
                ballY = ballRadius
                velocityY = -velocityY * 0.8f
            } else if (ballY > height - ballRadius) {
                ballY = height - ballRadius
                velocityY = -velocityY * 0.8f
            }

            if (Math.abs(ballX - lastBallX) > 0.5f || Math.abs(ballY - lastBallY) > 1) {
                if (ballPath.isEmpty) {
                    ballPath.moveTo(ballX, ballY)
                } else {
                    ballPath.lineTo(ballX, ballY)
                }
            }

            invalidate()
        } else {
            
            val curTime = System.currentTimeMillis()
            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                if (speed > shakeThreshold) {

                    val currentWidth = currentPaint.strokeWidth
                    val newWidth = if (currentWidth < maxStrokeWidth) {
                        currentWidth + 5f
                    } else {
                        baseStrokeWidth
                    }
                    setStrokeWidth(newWidth)
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }
}
