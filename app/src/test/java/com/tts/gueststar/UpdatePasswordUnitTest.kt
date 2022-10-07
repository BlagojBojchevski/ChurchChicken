package com.tts.gueststar

import com.tts.gueststar.ui.updatepassword.UpdatePasswordPresenter
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
class UpdatePasswordUnitTest {
    @InjectMocks
    private lateinit var presenter: UpdatePasswordPresenter
    @Mock
    lateinit var view: UpdatePasswordPresenter.UpdatePasswordView

    @Before
    @Throws
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = UpdatePasswordPresenter(view)
    }

    @Test
    fun validEmail_True() {
        Assert.assertTrue(presenter.updatePasswordalidationTest("aaaaaaa", "aaaaaaaa", "aaaaaaaa"))
    }

    @Test
    fun validEmail_False() {
        Assert.assertFalse(presenter.updatePasswordalidationTest("sds", "aaaaaaaa", "aaaaaaaa"))
    }


}