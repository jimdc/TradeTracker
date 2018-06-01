package com.advent.tradetracker.viewmodel

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.dialog_onboarding.*

class OnBoardingDialog : DialogFragment() , ViewPager.OnPageChangeListener{
    override fun onPageScrollStateChanged(state: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPageSelected(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val indicatorList = mutableListOf<ImageView>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.dialog_onboarding, container, false)
        val indicatorContainer = v?.findViewById<LinearLayout>(R.id.indicator_container)
        indicatorContainer?.let {
            if(fragmentList.size > 1)
                for (i in 0 until fragmentList.size) { addIndicator(indicatorContainer, i) }
        }
        return v
    }

    private fun  addIndicator (container : LinearLayout, position :Int){
        val indicator = LayoutInflater.from(context).inflate(R.layout.view_indicator, container,false)
        indicatorList.add(position,indicator as ImageView)
        container.addView(indicator)
    }

    private val fragmentList = mutableListOf<Fragment>()

    fun clearFragments() = fragmentList.clear()
    fun addFragment(fragment: Fragment) = fragmentList.add(fragment)
    fun addFragments(fragments : List<Fragment>) = fragmentList.addAll(fragments)

    private inner class OnBoardingAdapter(fm : FragmentManager): FragmentPagerAdapter(fm){
        override fun getItem(position: Int) =
                if (position < fragmentList.size ) fragmentList[position] else null
        override fun getCount(): Int = fragmentList.size

    }

    private var pagerAdapter : FragmentPagerAdapter? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagerAdapter = OnBoardingAdapter(childFragmentManager)

        view_pager_container?.adapter = pagerAdapter
        view_pager_container?.addOnPageChangeListener(this)
        view_pager_container?.currentItem = 0
        view_pager_container?.setBackgroundResource(R.color.primaryColor)

        onPageSelected(0)
    }


}