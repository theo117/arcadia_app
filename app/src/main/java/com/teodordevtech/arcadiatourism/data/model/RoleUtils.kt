package com.teodordevtech.arcadiatourism.data.model

fun String.normalizedRole(): String = trim().lowercase()

fun String.isTeacherRole(): Boolean = normalizedRole() in setOf("teacher", "admin")

fun String.isStudentRole(): Boolean = normalizedRole() == "student"
