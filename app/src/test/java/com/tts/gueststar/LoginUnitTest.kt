package com.tts.gueststar

import com.tts.gueststar.ui.login.ILoginView
import com.tts.gueststar.ui.login.LoginPresenter
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
class LoginUnitTest {
    @InjectMocks
    private lateinit var presenter: LoginPresenter
    @Mock
    lateinit var view: ILoginView

    @Before
    @Throws
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = LoginPresenter(view)
    }

    @Test
    fun loginValidationTest_True() {
        Assert.assertTrue(presenter.onLoginValidationTest("tester@gmail.com", "aaaaaaaa", true))
    }

    @Test
    fun loginValidationTest_False() {
        Assert.assertFalse(presenter.onLoginValidationTest("tester@gmail.com", "aaaaaaa", false))
    }


}