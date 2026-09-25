package com.saurabh.mediadminapp.utils
import io.github.cdimascio.dotenv.dotenv

val BASE_URL5 =dotenv()["PythonAnyWhere"].toString()
val BASE_URL3 = dotenv()["RenderAPI"].toString()
val BASE_URL = dotenv()["Emulator"].toString() // dev api for emulator
