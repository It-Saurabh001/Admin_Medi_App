package com.saurabh.mediadminapp.utils.utilityFunctions

fun String.capitalizeEachWord(): String =
    this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
