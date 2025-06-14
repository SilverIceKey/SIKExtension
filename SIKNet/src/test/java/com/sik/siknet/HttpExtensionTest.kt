import com.sik.siknet.http.httpGet
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpExtensionTest {
    data class Result(val msg: String)

    @Test
    fun testHttpGet() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("\{"msg\":\"ok\"\}"))
        server.start()
        val url = server.url("/test").toString()
        val result: Result = url.httpGet()
        assertEquals("ok", result.msg)
        server.shutdown()
    }
}
