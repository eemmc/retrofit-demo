package com.hbb.data.net

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class HttpFactory private constructor(
    private val json: Json
) : Converter.Factory() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return Converter<Any, RequestBody> {
            val typeSerializer = serializer(type)
            val result = json.encodeToString(typeSerializer, it)
            return@Converter RequestBody.create(MEDIA_TYPE, result)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any> {
            if (getRawType(type) == Result::class.java) {
                val resultType = getParameterUpperBound(
                    0, type as ParameterizedType
                )
                try {
                    val data = Json.parseToJsonElement(it.string())
                    val code = data.jsonObject["code"]?.jsonPrimitive?.intOrNull ?: 0
                    if (code in 200 until 300) {
                        val body = data.jsonObject["data"]
                        if (body != null) {
                            val typeSerializer = serializer(resultType)
                            val res = json.decodeFromJsonElement(typeSerializer, body)
                            return@Converter Result.success(res)
                        } else {
                            return@Converter Result.success(null)
                        }
                    } else {
                        val msg = data.jsonObject["msg"]?.jsonPrimitive?.contentOrNull ?: ""
                        return@Converter Result.failure<Any>(HttpException(msg, code))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@Converter Result.failure<Any>(e)
                }
            } else {
                return@Converter Result.failure<Any>(HttpException("Network Error"))
            }
        }
    }

    companion object {
        private val MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8")

        fun create(): HttpFactory {
            return create(Json {
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
        }

        @Suppress("ConstantConditions")
        fun create(json: Json): HttpFactory {
            return HttpFactory(json)
        }

    }
}