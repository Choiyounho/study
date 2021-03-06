package com.soten.fooddelivery.screen.main.home.restaurant.detail

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.soten.fooddelivery.R
import com.soten.fooddelivery.data.entity.RestaurantEntity
import com.soten.fooddelivery.data.entity.RestaurantFoodEntity
import com.soten.fooddelivery.databinding.ActivityRestaurantDetailBinding
import com.soten.fooddelivery.extensions.fromDpToPx
import com.soten.fooddelivery.extensions.load
import com.soten.fooddelivery.screen.base.BaseActivity
import com.soten.fooddelivery.screen.main.MainActivity
import com.soten.fooddelivery.screen.main.MainTabMenu
import com.soten.fooddelivery.screen.main.home.restaurant.RestaurantListFragment
import com.soten.fooddelivery.screen.main.home.restaurant.detail.menu.RestaurantMenuListFragment
import com.soten.fooddelivery.screen.main.home.restaurant.detail.review.RestaurantReviewListFragment
import com.soten.fooddelivery.screen.order.OrderMenuListActivity
import com.soten.fooddelivery.util.event.MenuChangeEventBus
import com.soten.fooddelivery.widget.adapter.RestaurantDetailListFragmentPagerAdapter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.Math.abs

class RestaurantDetailActivity :
    BaseActivity<RestaurantDetailViewModel, ActivityRestaurantDetailBinding>() {

    override val viewModel by viewModel<RestaurantDetailViewModel> {
        parametersOf(
            intent.getParcelableExtra<RestaurantEntity>(RestaurantListFragment.RESTAURANT_KEY)
        )
    }

    override fun getViewBinding() = ActivityRestaurantDetailBinding.inflate(layoutInflater)

    override fun initViews() {
        initAppBar()
    }

    private val auth by lazy { Firebase.auth }

    private val menuChangeEventBus by inject<MenuChangeEventBus>()

    private lateinit var viewPagerAdapter: RestaurantDetailListFragmentPagerAdapter

    private fun initAppBar() = with(binding) {
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val topPadding = 300f.fromDpToPx().toFloat()
            val realAlphaScrollHeight = appBarLayout.measuredHeight - appBarLayout.totalScrollRange
            val abstractOffset = abs(verticalOffset)

            val realAlphaVerticalOffset: Float =
                if (abstractOffset - topPadding < 0) 0f else abstractOffset - topPadding

            if (abstractOffset < topPadding) {
                restaurantTitleTextView.alpha = 0f
                return@OnOffsetChangedListener
            }

            val percentage = realAlphaVerticalOffset / realAlphaScrollHeight
            restaurantTitleTextView.alpha =
                1 - (if (1 - percentage * 2 < 0) 0f else (1 - percentage) * 2)
        })
        toolbar.setNavigationOnClickListener { finish() }
        callButton.setOnClickListener {
            viewModel.getRestaurantTelNumber()?.let { telNumber ->
                if (telNumber.length < 3) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.nonexistent_tel_number),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telNumber"))
                )
            }
        }
        likeButton.setOnClickListener {
            viewModel.toggleLikeRestaurant()
        }
        shareButton.setOnClickListener {
            viewModel.getRestaurantInformation()?.let { restaurantInformation ->
                startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = MIMETYPE_TEXT_PLAIN
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "????????? ????????? : ${restaurantInformation.restaurantTitle}" +
                                    "\n?????? : ${restaurantInformation.grade}" +
                                    "\n????????? : ${restaurantInformation.restaurantTelNumber}"
                        )
                        Intent.createChooser(this, "???????????? ????????????")
                    }
                )
            }
        }
    }

    override fun observeData() = viewModel.restaurantDetailStateLiveData.observe(this) { state ->
        when (state) {
            is RestaurantDetailState.Loading -> handleLoading()
            is RestaurantDetailState.Success -> {
                handleSuccess(state)
            }
            else -> {
            }
        }
    }

    private fun handleLoading() {
        binding.progressBar.isGone = true
    }

    private fun handleSuccess(state: RestaurantDetailState.Success) = with(binding) {
        progressBar.isGone = true

        val restaurantEntity = state.restaurantEntity

        callButton.isGone = restaurantEntity.restaurantTelNumber == null

        restaurantTitleTextView.text = restaurantEntity.restaurantTitle
        restaurantImage.load(restaurantEntity.restaurantImageUrl)
        restaurantMainTitleText.text = restaurantEntity.restaurantTitle
        ratingBar.rating = restaurantEntity.grade
        deliveryTimeText.text =
            getString(
                R.string.delivery_expected_time,
                restaurantEntity.deliveryTimeRange.first,
                restaurantEntity.deliveryTimeRange.second
            )
        deliveryTipText.text =
            getString(
                R.string.delivery_tip_range,
                restaurantEntity.deliveryTipRange.first,
                restaurantEntity.deliveryTipRange.second
            )

        likeText.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                this@RestaurantDetailActivity, if (state.isLiked == true) {
                    R.drawable.ic_heart_enable
                } else {
                    R.drawable.ic_heart_disable
                }
            ),
            null, null, null
        )

        if (::viewPagerAdapter.isInitialized.not()) {
            initViewPager(
                state.restaurantEntity.restaurantInfoId,
                state.restaurantEntity.restaurantTitle,
                state.restaurantFoodList
            )
        }

        notifyBasketCount(state.foodMenuListInBasket)

        val (isClearNeed, afterAction) = state.isClearNeedInBasketAndAction
        if (isClearNeed) {
            alertClearNeedInBasket(afterAction)
        }
    }

    private fun initViewPager(
        restaurantInfoId: Long,
        restaurantTitle: String,
        restaurantFoodList: List<RestaurantFoodEntity>?
    ) {
        viewPagerAdapter = RestaurantDetailListFragmentPagerAdapter(
            this,
            listOf(
                RestaurantMenuListFragment.newInstance(
                    restaurantInfoId,
                    ArrayList(restaurantFoodList ?: listOf())
                ),
                RestaurantReviewListFragment.newInstance(
                    restaurantTitle
                )
            )
        )
        binding.menuAndReviewViewPager.adapter = viewPagerAdapter
        TabLayoutMediator(
            binding.menuAndReviewTabLayout,
            binding.menuAndReviewViewPager
        ) { tab, position ->
            tab.setText(RestaurantDetailCategory.values()[position].categoryNameId)
        }.attach()
    }

    private fun notifyBasketCount(foodMenuListInBasket: List<RestaurantFoodEntity>?) = with(binding) {
        basketCountTextView.text = if (foodMenuListInBasket.isNullOrEmpty()) {
            "0"
        } else {
            getString(R.string.basket_count, foodMenuListInBasket.size)
        }
        basketButton.setOnClickListener {
            if (auth.currentUser == null) {
                alertLoginNeed {
                    lifecycleScope.launch {
                        menuChangeEventBus.changeMenu(MainTabMenu.MY)
                        finish()
                    }
                }
            } else {
                startActivity(
                    OrderMenuListActivity.newIntent(this@RestaurantDetailActivity)
                )
            }
        }
    }

    private fun alertLoginNeed(action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("???????????? ???????????????.")
            .setMessage("??????????????? ???????????? ???????????????. My????????? ?????????????????????????")
            .setPositiveButton("??????") { dialog, _ ->
                action()
                dialog.dismiss()
            }
            .setNegativeButton("??????") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun alertClearNeedInBasket(afterAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("?????????????????? ?????? ????????? ????????? ?????? ??? ????????????.")
            .setMessage("???????????? ????????? ??????????????? ?????? ?????? ????????? ?????? ????????? ???????????????.")
            .setPositiveButton("??????") { dialog, _ ->
                viewModel.notifyClearBasket()
                afterAction()
                dialog.dismiss()
            }
            .setNegativeButton("??????") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {

        fun newIntent(context: Context, restaurantEntity: RestaurantEntity) =
            Intent(context, RestaurantDetailActivity::class.java).apply {
                putExtra(RestaurantListFragment.RESTAURANT_KEY, restaurantEntity)
            }
    }
}