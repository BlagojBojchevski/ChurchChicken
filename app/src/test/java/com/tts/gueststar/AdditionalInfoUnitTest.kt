package com.tts.gueststar

import android.content.Context
import com.tts.gueststar.ui.signuppresenter.AdditionalInfoPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class AdditionalInfoUnitTest {
    @InjectMocks
    private lateinit var presenter: AdditionalInfoPresenter
    @Mock
    lateinit var view: AdditionalInfoPresenter.AdditionalInfoInterface

    private var context = mock(Context::class.java)
    @Before
    @Throws
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = AdditionalInfoPresenter(context, view)
    }

    @Test
    fun loginValidationTest_True() {
        Assert.assertTrue(
            presenter.checkRequirementsTest(
                "tester@gmail.com",
                "aaaaaaaa",
                "555-444-8888",
                "23456",
                12332,
                "sad",
                "sad",
                true
            )
        )
    }

    @Test
    fun loginValidationTest_False() {
        Assert.assertFalse(
            presenter.checkRequirementsTest(
                "tester.com",
                "aaaaaaaa",
                "5554448888",
                "23456",
                12332,
                "sad",
                "sad",
                true
            )
        )

    }


}