import com.hbb.data.net.Repository
import com.hbb.data.net.request.Register
import com.hbb.data.net.service.HttpApi
import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    withContext(Dispatchers.IO) {
        Repository.getInstance()[HttpApi::class.java]
            //.query(Register("13162705828", "18349276", "335227"))
            .check()
    }.fold(onFailure = {
        println("failure: $it")
    }, onSuccess = {
        println("success: $it")
    })
}