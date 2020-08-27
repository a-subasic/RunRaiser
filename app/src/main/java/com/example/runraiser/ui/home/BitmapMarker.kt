package com.example.runraiser.ui.home

import android.graphics.*

class BitmapMarker {
    companion object {
//        fun getBitmapFromUrl(url: String): Bitmap {
//
//        }

        fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
//         canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle(
                (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                (bitmap.width / 2).toFloat(), paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        fun addBorderToCircularBitmap(
            srcBitmap: Bitmap,
            borderWidth: Float,
            borderColor: Int
        ): Bitmap {
            // Calculate the circular bitmap width with border
            val dstBitmapWidth = srcBitmap.width + borderWidth * 2
            // Initialize a new Bitmap to make it bordered circular bitmap
            val dstBitmap =
                Bitmap.createBitmap(dstBitmapWidth.toInt(), dstBitmapWidth.toInt(), Bitmap.Config.ARGB_8888)
            // Initialize a new Canvas instance
            val canvas = Canvas(dstBitmap)
            // Draw source bitmap to canvas
            canvas.drawBitmap(srcBitmap, borderWidth, borderWidth, null)
            // Initialize a new Paint instance to draw border
            val paint = Paint()
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth.toFloat()
            paint.isAntiAlias = true
            canvas.drawCircle(
                (canvas.width / 2).toFloat(),  // cx
                (canvas.width / 2).toFloat(),  // cy
                (canvas.width / 2 - borderWidth / 2).toFloat(),  // Radius
                paint // Paint
            )
            // Free the native object associated with this bitmap.
            srcBitmap.recycle()
            // Return the bordered circular bitmap
            return dstBitmap
        }
    }
}