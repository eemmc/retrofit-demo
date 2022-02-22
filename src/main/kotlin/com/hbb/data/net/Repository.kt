package com.hbb.data.net

import java.lang.reflect.Type

class Repository private constructor() {
    private val server = HttpService.create()
    private val cache = HashMap<Type, Any>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(type: Class<T>): T {
        return if (cache.containsKey(type)) {
            cache[type] as T
        } else {
            val api = server.loadService(type)
            cache[type] = api!!
            api
        }
    }

    companion object {
        private var sInstance: Repository? = null
        fun getInstance(): Repository {
            if (sInstance == null) {
                synchronized(Repository::class.java) {
                    if (sInstance == null) {
                        sInstance = Repository()
                    }
                }
            }
            return sInstance!!
        }

    }
}