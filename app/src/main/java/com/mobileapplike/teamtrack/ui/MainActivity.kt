package com.mobileapplike.teamtrack.ui


import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.mobileapplike.teamtrack.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    lateinit var mToolbar: Toolbar
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawer : DrawerLayout;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        mToolbar = findViewById(R.id.toolbar) as Toolbar
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar)

        mDrawer = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerToggle = ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        mDrawer.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
        mDrawerToggle.setDrawerArrowDrawable( HamburgerDrawable(this));
  }



    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled)
            DrawerLayout.LOCK_MODE_UNLOCKED
        else
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        mDrawer.setDrawerLockMode(lockMode)
        mDrawerToggle.setDrawerIndicatorEnabled(enabled)

        if (!enabled) {
            supportActionBar?.title = ""
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }


}

class HamburgerDrawable(context: Context) : DrawerArrowDrawable(context) {
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        barLength = 80.0f
        barThickness = 10.0f
        gapSize = 15.0f
    }

    init {
        //color = context.getResources().getColor(R.color.white)
    }
}