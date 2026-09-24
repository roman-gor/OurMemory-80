package com.gorman.ourmemoryapp.data.auth.mapper

import com.google.firebase.auth.FirebaseUser
import com.gorman.ourmemoryapp.data.auth.model.AuthUser

fun FirebaseUser.toAuthUser() = AuthUser(
    uid = uid,
    email = email.orEmpty(),
    isAnonymous = isAnonymous
)
