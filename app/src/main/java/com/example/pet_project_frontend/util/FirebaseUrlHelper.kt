package com.example.pet_project_frontend.util

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder

/**
 * Firebase Storage URL 헬퍼
 * 
 * 만료된 signed URL에서 파일 경로를 추출하여
 * 새로운 다운로드 URL을 생성합니다.
 */
object FirebaseUrlHelper {
    private const val TAG = "FirebaseUrlHelper"
    
    /**
     * Firebase Storage URL에서 유효한 다운로드 URL을 가져옵니다.
     * 
     * @param url 원본 URL (만료된 signed URL 또는 storage.googleapis.com URL)
     * @return 새로 생성된 다운로드 URL
     */
    suspend fun getValidDownloadUrl(url: String): String {
        return try {
            // URL에서 파일 경로 추출
            val filePath = extractFilePath(url)
            if (filePath == null) {
                Log.w(TAG, "Could not extract file path from URL: $url")
                return url
            }
            
            Log.d(TAG, "Attempting to refresh URL for path: $filePath")
            
            // Firebase Storage에서 항상 새 다운로드 URL 생성
            val storage = FirebaseStorage.getInstance()
            Log.d(TAG, "Using Firebase Storage bucket: ${storage.reference.bucket}")
            
            val storageRef = storage.reference.child(filePath)
            Log.d(TAG, "Storage reference path: ${storageRef.path}")
            
            val downloadUrl = storageRef.downloadUrl.await()
            
            Log.d(TAG, "Successfully generated fresh download URL")
            downloadUrl.toString()
        } catch (e: com.google.firebase.storage.StorageException) {
            // Firebase Storage 특정 에러 (파일 없음, 권한 없음 등)
            Log.e(TAG, "StorageException for path: ${extractFilePath(url)}, code: ${e.errorCode}, message: ${e.message}")
            // 원본 URL 반환 (만료되었지만 서버에서 처리 가능할 수 있음)
            url
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get valid download URL for: $url", e)
            // 실패 시 원본 URL 반환 (오프라인 상태 등)
            url
        }
    }
    
    /**
     * URL에서 파일 경로를 추출합니다.
     * 
     * 예시:
     * - https://firebasestorage.googleapis.com/v0/b/bucket.appspot.com/o/path%2Fto%2Ffile.jpg?alt=media&token=xxx
     *   → path/to/file.jpg
     * - https://storage.googleapis.com/bucket-name/path/to/file.jpg
     *   → path/to/file.jpg
     */
    private fun extractFilePath(url: String): String? {
        return try {
            val result = when {
                // Firebase Storage API URL
                url.contains("/v0/b/") && url.contains("/o/") -> {
                    // /o/ 이후의 경로 추출
                    val afterO = url.substringAfter("/o/")
                    val encodedPath = afterO.substringBefore("?")
                    URLDecoder.decode(encodedPath, "UTF-8")
                }
                // 직접 Storage URL
                // https://storage.googleapis.com/bucket-name/path/to/file.jpg
                url.contains("storage.googleapis.com/") -> {
                    // storage.googleapis.com/ 이후를 가져옴
                    val afterDomain = url.substringAfter("storage.googleapis.com/")
                    // 첫 번째 슬래시 이후가 실제 파일 경로 (버킷 이름 제거)
                    val pathAfterBucket = afterDomain.substringAfter("/", "")
                    if (pathAfterBucket.isEmpty()) {
                        // 슬래시가 없으면 전체가 경로
                        afterDomain.substringBefore("?")
                    } else {
                        pathAfterBucket.substringBefore("?")
                    }
                }
                else -> null
            }
            
            Log.d(TAG, "Extracted path: $result from URL: $url")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting file path from URL: $url", e)
            null
        }
    }
    
    /**
     * 여러 URL을 일괄 처리합니다.
     */
    suspend fun getValidDownloadUrls(urls: List<String>): List<String> {
        return urls.map { getValidDownloadUrl(it) }
    }
}
