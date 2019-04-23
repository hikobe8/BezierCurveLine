package com.ray.beziercurveline

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Author : hikobe8@github.com
 * Time : 2019/4/21 11:45 PM
 * Description :
 */
class BezierView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        const val SMOOTHNESS = 0.5f
    }

    private val mValuePointList: ArrayList<PointF> = ArrayList()
    private val mControlPointList: ArrayList<PointF> = ArrayList()

    init {
        calculateValuePoint(arrayListOf(Item(8f), Item(1f), Item(3f), Item(6f), Item(0.5f)), 10f, 300f, 100f)
        calculateControlPoint(mValuePointList)
    }

    private fun calculateValuePoint(itemList: List<Item>, max: Float, scaleX: Float, scaleY: Float) {
        mValuePointList.clear()
        for ((i, item) in itemList.withIndex()) {
            val x = i * scaleX
            val y = (max - item.value) * scaleY
            mValuePointList.add(PointF(x, y))
        }
    }

    private fun calculateControlPoint(pointList: List<PointF>) {
        mControlPointList.clear()
        if (pointList.size <= 1) {
            return
        }
        for ((i, point) in pointList.withIndex()) {
            when (i) {
                0 -> {//第一项
                    //添加后控制点
                    val nextPoint = pointList[i + 1]
                    val controlX = point.x + (nextPoint.x - point.x) * SMOOTHNESS
                    val controlY = point.y
                    mControlPointList.add(PointF(controlX, controlY))
                }
                pointList.size - 1 -> {//最后一项
                    //添加前控制点
                    val lastPoint = pointList[i - 1]
                    val controlX = point.x - (point.x - lastPoint.x) * SMOOTHNESS
                    val controlY = point.y
                    mControlPointList.add(PointF(controlX, controlY))
                }
                else -> {//中间项
                    val lastPoint = pointList[i - 1]
                    val nextPoint = pointList[i + 1]
                    val k = (nextPoint.y - lastPoint.y) / (nextPoint.x - lastPoint.x)
                    val b = point.y - k * point.x
                    //添加前控制点
                    val lastControlX = point.x - (point.x - lastPoint.x) * SMOOTHNESS
                    val lastControlY = k * lastControlX + b
                    mControlPointList.add(PointF(lastControlX, lastControlY))
                    //添加后控制点
                    val nextControlX = point.x + (nextPoint.x - point.x) * SMOOTHNESS
                    val nextControlY = k * nextControlX + b
                    mControlPointList.add(PointF(nextControlX, nextControlY))
                }
            }
        }
    }

    private val mPath = Path()

    private val mPaint = Paint()
    private lateinit var mShader: LinearGradient

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mShader =
                LinearGradient(0F, 0F, 0F, h.toFloat(), Color.GREEN, Color.TRANSPARENT, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //连接各部分曲线
        mPath.reset()
        val firstPoint = mValuePointList.first()
        mPath.moveTo(firstPoint.x, height.toFloat())
        mPath.lineTo(firstPoint.x, firstPoint.y)
        for (i in 0 until mValuePointList.size * 2 - 2 step 2) {
            val leftControlPoint = mControlPointList[i]
            val rightControlPoint = mControlPointList[i + 1]
            val rightPoint = mValuePointList[i / 2 + 1]
            mPath.cubicTo(
                leftControlPoint.x,
                leftControlPoint.y,
                rightControlPoint.x,
                rightControlPoint.y,
                rightPoint.x,
                rightPoint.y
            )
        }
        val lastPoint = mValuePointList.last()
//填充渐变色
        mPath.lineTo(lastPoint.x, height.toFloat())
        mPath.lineTo(firstPoint.x, height.toFloat())
        mPaint.alpha = 255
        mPaint.style = Paint.Style.FILL
        mPaint.shader = mShader
        canvas?.drawPath(mPath, mPaint)
//绘制全部路径
        mPath.setLastPoint(lastPoint.x, height.toFloat())
        mPaint.strokeWidth = 2f
        mPaint.style = Paint.Style.STROKE
        mPaint.shader = null
        mPaint.color = Color.RED
        canvas?.drawPath(mPath, mPaint)
        for (i in 0 until mValuePointList.size) {
            val point = mValuePointList[i]
            //画数值线
            mPaint.color = Color.GREEN
            mPaint.alpha = 100
            canvas?.drawLine(point.x, point.y, point.x, height.toFloat(), mPaint)
            //画数值点
            mPaint.style = Paint.Style.FILL
            mPaint.alpha = 255
            canvas?.drawCircle(point.x, point.y, 10f, mPaint)
        }
    }

}