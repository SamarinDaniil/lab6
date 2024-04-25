package com.example.lab6

class DataSingleton private constructor() {

    private var storedString: String? = null

    init {
        // Пример инициализации значения по умолчанию
        storedString = "Update"
    }

    fun setString(value: String) {
        storedString = value
    }

    fun getString(): String? {
        return storedString
    }

    companion object {
        @Volatile
        private var instance: DataSingleton? = null

        fun getInstance(): DataSingleton {
            return instance ?: synchronized(this) {
                instance ?: DataSingleton().also { instance = it }
            }
        }
    }
}