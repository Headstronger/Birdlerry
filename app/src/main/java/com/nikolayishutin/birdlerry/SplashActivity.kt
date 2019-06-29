package com.nikolayishutin.birdlerry

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    internal var SPLASH_TIME_OUT = 800
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            if (!checkSelfPermission()) {
                requestPermission()
            }
        }, SPLASH_TIME_OUT.toLong())
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(request: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (request) {
            6036 -> {
                val permissionGranted: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (permissionGranted) {
                    loadAllImages()
                } else
                    Toast.makeText(this, "Permission Denied! Cannot load images.", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(request, permissions, grantResults)
    }

    private fun loadAllImages() {
        var imagesList = getAllShownImagesPath(this)
        var intent = Intent(this, MainActivity::class.java)
        intent.putParcelableArrayListExtra("image_url_data", imagesList)
        startActivity(intent)
        finish()
    }

    private fun getAllShownImagesPath(activity: Activity): ArrayList<out Albums>? {
        val uri: Uri
        val cursor: Cursor?
        var cursorBucket: Cursor?
        val column_index_data: Int
        val column_index_folder_name: Int
        val listOfAllImages = ArrayList<String>()
        var absolutePathOfImage: String? = null
        var albumsList = ArrayList<Albums>()
        var album: Albums? = null

        val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
        val BUCKET_ORDER_BY = "MAX(datetaken) DESC"

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATA
        )
        cursor = activity.contentResolver.query(uri, projection, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY)

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data)
                Log.d("title_apps", "bucket name:" + cursor.getString(column_index_data))
                val selectionArgs = arrayOf("%" + cursor.getString(column_index_folder_name))
                val selection = MediaStore.Images.Media.DATA + "like ? "
                val projectionOnlyBucket =
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                cursorBucket = activity.contentResolver.query(uri, projectionOnlyBucket, selection, selectionArgs, null)
                Log.d("title_apps", "bucket size:$cursorBucket")
                if (absolutePathOfImage != "" && absolutePathOfImage != null) {
                    listOfAllImages.add(absolutePathOfImage)
                    if (cursorBucket != null) {
                        albumsList.add(
                            Albums(
                                cursor.getString(column_index_folder_name),
                                absolutePathOfImage,
                                cursorBucket.count,
                                false
                            )
                        )
                    }
                }
            }
        }

        return getListOfVideoFolders(albumsList)
    }

    private fun getListOfVideoFolders(albumsList: ArrayList<Albums>): ArrayList<out Albums>? {
        var cursor: Cursor?
        var cursorBucket: Cursor?
        var uri: Uri
        val BUCKET_GROUP_BY = "1) GROUP BY 1, (2"
        val BUCKET_ORDER_BY = "MAX(datetaken) DESC"
        val column_index_album_name: Int
        val column_index_album_video: Int

        uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DATA
        )
        cursor = this.contentResolver.query(uri, projection, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY)
        if (cursor != null) {
            column_index_album_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            column_index_album_video = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_name))
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_video))
                val selectionArgs = arrayOf("%" + cursor.getString(column_index_album_name) + "%")

                val selection = MediaStore.Video.Media.DATA+" like ? "
                val projectionOnlyBucket = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                cursorBucket=this.contentResolver.query(uri,projectionOnlyBucket,selection,selectionArgs, null)
                if (cursorBucket != null) {
                    Log.d("title_apps", "bucket size:" + cursorBucket.count)
                }
                if (cursorBucket != null) {
                    albumsList.add(Albums(cursor.getString(column_index_album_name),cursor.getString(column_index_album_video),cursorBucket.count, true))
                }
            }
        }
        return albumsList
    }
}

