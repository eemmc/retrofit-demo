package com.hbb.data.net

import okhttp3.*
import okhttp3.Interceptor
import retrofit2.Retrofit
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HttpService private constructor() {
    private var mRetrofit: Retrofit

    init {
        val sslFactory = try {
            val sc = SSLContext.getInstance("TLS")
            val tm = arrayOf<TrustManager>(DefaultTrustManager())
            sc.init(null, tm, SecureRandom())
            sc.socketFactory
        } catch (e: java.lang.Exception) {
            throw IllegalAccessError()
        }

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslFactory, DefaultTrustManager())
            .hostnameVerifier(UnCheckedHostnameVerifier())
            .addInterceptor(NonExceptionInterceptor())
            .cookieJar(CookiesStore())
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        mRetrofit = Retrofit.Builder()
            .baseUrl(SERVICE_HOSTNAME)
            .addConverterFactory(HttpFactory.create())
            .client(client)
            .build()
    }

    fun <T> loadService(type: Class<T>): T {
        return mRetrofit.create(type)
    }

    companion object {
        private const val SERVICE_HOSTNAME = "http://127.0.0.1:8088"

        fun create(): HttpService {
            return HttpService()
        }
    }

    private class CookiesStore : CookieJar {
        private val mMemoryCookies = ConcurrentHashMap<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
            mMemoryCookies[url.host()] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = mMemoryCookies[url.host()]
            return cookies ?: ArrayList()
        }
    }

    private class DefaultTrustManager : X509TrustManager {

        override fun checkClientTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) = Unit

        override fun checkServerTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private class UnCheckedHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }

    private class NonExceptionInterceptor : Interceptor {
        private val type = MediaType.get("application/json; charset=UTF-8")
        private val data = """{"code":%d,"msg":"%s"}"""
        private val ignore = arrayOf(204, 205)

        private fun wrap(response: Response): Response {
            val builder = response.newBuilder()

            var body = response.body()
            if (body == null || body.contentLength() == 0L) {
                body = ResponseBody.create(
                    type, data.format(response.code(), response.message())
                )
                builder.body(body)
            }

            if (response.code() in ignore) {
                builder.code(203)
            }

            return builder.build()
        }

        private fun swap(response: Response): Response {
            var body = response.body()
            if (body == null || body.contentLength() == 0L) {
                body = ResponseBody.create(
                    type, data.format(response.code(), response.message())
                )
            }
            return response.newBuilder()
                .code(203)
                .body(body)
                .build()
        }

        private fun build(request: Request, ex: Throwable): Response {
            val body = ResponseBody.create(
                type, data.format(0, ex.toString())
            )
            return Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .message("Error")
                .code(203)
                .body(body)
                .build()
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val res = try {
                val response = chain.proceed(request)
                if (response.code() in 200 until 300) {
                    wrap(response)
                } else {
                    swap(response)
                }
            } catch (e: IOException) {
                build(request, e)
            }

            return res
        }
    }
}