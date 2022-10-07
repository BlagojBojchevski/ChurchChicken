package com.tts.gueststar

import com.tts.gueststar.ui.promocode.PromoCodePresenter
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
class PromoUnitTest {
    @InjectMocks
    private lateinit var presenter: PromoCodePresenter
    @Mock
    lateinit var view: PromoCodePresenter.PromoCodeView

    @Before
    @Throws
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = PromoCodePresenter(view)
    }

    @Test
    fun promoCodeTest_True() {
        Assert.assertTrue(presenter.onPromoCodeValidateionTest("12345"))
    }

    @Test
    fun promoCodeTest_False() {
        Assert.assertFalse(presenter.onPromoCodeValidateionTest("1"))
    }


}