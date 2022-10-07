package com.tts.gueststar

import com.tts.gueststar.ui.trasfercard.TrasferCardPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TransferCardTest {
    @InjectMocks
    private lateinit var presenter: TrasferCardPresenter
    @Mock
    lateinit var view: TrasferCardPresenter.TransferCardView

    @Before
    @Throws
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = TrasferCardPresenter(view)
    }

    @Test
    fun transferCardTest_True() {
        Assert.assertTrue(presenter.onTransferCardValidateionTest("1222222223232345","1222222223232345"))
    }

    @Test
    fun transferCardTest_False() {
        Assert.assertFalse(presenter.onTransferCardValidateionTest("1222222223232345","234"))
    }


}