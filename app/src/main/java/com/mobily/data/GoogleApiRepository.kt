package com.mobily.data

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class GoogleApiRepository {
    fun uploadImage(imageUri: Uri?, success: (String) -> Unit, error: (String) -> Unit) {
        if (imageUri != null) {
            val fileReference = "images/${imageUri.lastPathSegment}.jpg"
            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child(fileReference)
            ref.putFile(imageUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    it?.let { it1 -> success.invoke(it1.toString()) }
                }
                it.uploadSessionUri?.path?.let { it1 ->  }
            }.addOnFailureListener {
                it.message?.let { it1 -> error.invoke(it1) }
            }
        }
    }
}