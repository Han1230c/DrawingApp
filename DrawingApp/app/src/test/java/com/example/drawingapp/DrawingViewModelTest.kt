import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.drawingapp.Drawing
import com.example.drawingapp.DrawingDao
import com.example.drawingapp.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import io.mockk.mockkStatic
import io.mockk.every
import android.util.Log
import com.example.drawingapp.MainDispatcherRule

@ExperimentalCoroutinesApi
class DrawingViewModelTest {

    // 使用 MainDispatcherRule 确保测试中替换了 Dispatchers.Main
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 使用 InstantTaskExecutorRule 使得 LiveData 能够同步更新
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var drawingDao: DrawingDao

    @Mock
    private lateinit var observer: Observer<List<Drawing>>

    @Captor
    private lateinit var drawingCaptor: ArgumentCaptor<Drawing>

    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setup() {
        // 模拟 Android 的 Log 类，避免未模拟方法异常
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        // 初始化 Mockito 和 ViewModel
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = DrawingViewModel(drawingDao)

        // 观察 LiveData
        viewModel.allDrawings.observeForever(observer)
    }

    @After
    fun tearDown() {
        // 清除调度器
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllDrawings should update allDrawings LiveData`() = testScope.runTest {
        // 准备模拟的数据
        val drawings = listOf(Drawing(1, "Test Drawing", "[]", ""))
        `when`(drawingDao.getAllDrawings()).thenReturn(drawings)

        // 执行 ViewModel 中的方法
        viewModel.loadAllDrawings()
        advanceUntilIdle() // 确保协程完成所有操作

        // 验证观察者是否接收到正确的数据
        verify(observer).onChanged(drawings)
    }

    @Test
    fun `saveDrawing should insert drawing into database`() = testScope.runTest {
        // 准备插入的数据
        val drawing = Drawing(0, "New Drawing", "[]", "")

        // 调用 ViewModel 的保存方法
        viewModel.saveDrawing(drawing.name, emptyList())
        advanceUntilIdle() // 确保协程完成所有操作

        // 验证 DAO 的 insertDrawing 方法是否被调用
        verify(drawingDao).insertDrawing(drawingCaptor.capture())

        // 验证捕获到的插入数据是否与预期一致
        val capturedDrawing = drawingCaptor.value
        assert(capturedDrawing.name == "New Drawing")
    }
}
