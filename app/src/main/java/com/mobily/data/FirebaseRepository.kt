package com.mobily.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseRepository {
    /*
    * This method will upload image to firebase
    * */
    fun uploadImage(imageUri: Uri?, success: (String) -> Unit, error: (String) -> Unit) {
        if (imageUri != null) {
            val fileReference = "images/${imageUri.lastPathSegment}.jpg"
            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child(fileReference)
            ref.putFile(imageUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    it?.let { it1 -> success.invoke(it1.toString()) }
                }
            }.addOnFailureListener {
                it.message?.let { it1 -> error.invoke(it1) }
            }
        }
    }
}