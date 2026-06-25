import com.sik.siknet.http.HttpUtils
import com.sik.siknet.http.NetException
import com.sik.siknet.http.httpGet
import com.sik.siknet.http.httpPostForm
import com.sik.siknet.http.toMap
import com.tencent.mmkv.MMKV
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class HttpExtensionTest {
    data class Result(val msg: String)
    data class FormParams(
        val name: String,
        val age: Int,
        val active: Boolean,
        val note: String?
    )

    private var previousLoggerInRequest = true
    private lateinit var previousNetExceptionHandler: (Request, NetException) -> Boolean

    @Before
    fun setUp() {
        previousLoggerInRequest = HttpUtils.isLoggerInRequest
        previousNetExceptionHandler = HttpUtils.globalNetExceptionHandler
        mockkStatic(MMKV::class)
        val mmkv = mockk<MMKV>(relaxed = true)
        every { MMKV.defaultMMKV() } returns mmkv
        every { mmkv.decodeString(any(), any()) } returns ""
        HttpUtils.isLoggerInRequest = false
        HttpUtils.globalNetExceptionHandler = { _, ex -> throw ex }
    }

    @After
    fun tearDown() {
        HttpUtils.isLoggerInRequest = previousLoggerInRequest
        HttpUtils.globalNetExceptionHandler = previousNetExceptionHandler
        unmockkStatic(MMKV::class)
    }

    @Test
    fun testHttpGet() {
        val server = MockWebServer()
        try {
            server.enqueue(MockResponse().setBody("""{"msg":"ok"}"""))
            server.start()
            val url = server.url("/test").toString()
            val result: Result = url.httpGet()
            assertEquals("ok", result.msg)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun testToMapKeepsOriginalValueTypes() {
        val params = FormParams(
            name = "Ada",
            age = 18,
            active = true,
            note = null
        )

        val map = params.toMap()

        assertEquals("Ada", map["name"])
        assertEquals(18, map["age"])
        assertEquals(true, map["active"])
        assertNull(map["note"])
    }

    @Test
    fun testHttpPostFormConvertsObjectValuesWhenBuildingFormBody() {
        val server = MockWebServer()
        try {
            server.enqueue(MockResponse().setBody("""{"msg":"ok"}"""))
            server.start()
            val url = server.url("/form").toString()

            val result: Result = url.httpPostForm(
                FormParams(
                    name = "Ada",
                    age = 18,
                    active = true,
                    note = null
                )
            )
            val bodyParts = server.takeRequest().body.readUtf8().split("&").toSet()

            assertEquals("ok", result.msg)
            assertEquals(setOf("active=true", "age=18", "name=Ada", "note="), bodyParts)
        } finally {
            server.shutdown()
        }
    }
}
