package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
): String {
    val payload = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.name.lowercase())
        put("path", path)
        if (exception is FirebaseFirestoreException) {
            put("code", exception.code.name)
        }
    }.toString()

    Log.e("FirestoreError", payload, exception)
    return when {
        exception is FirebaseFirestoreException &&
            exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "云端权限校验失败，请确认登录状态后重试"
        exception is FirebaseFirestoreException &&
            exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "网络连接不稳定，已自动保留本地缓存，联网后将自动同步"
        else -> "云端同步暂遇异常，请稍后下拉刷新重试"
    }
}
