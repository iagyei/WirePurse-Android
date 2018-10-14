package com.transcodium.tnsmoney

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.transcodium.app.DrawerListAdapter
import com.transcodium.app.DrawerListModel
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.transcodium.tnsmoney.classes.Account
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.navigation_drawer.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import org.jetbrains.anko.*
import org.json.JSONObject
import kotlin.coroutines.experimental.CoroutineContext


open class DrawerActivity : AppCompatActivity(), CoroutineScope {


     var  job: Job? = null

    override val coroutineContext: CoroutineContext
                    get() = Dispatchers.Main

    val mActivity by lazy{
        this
    }

    //lazy init vals
    private val toolbar: Toolbar by lazy{
        findViewById<Toolbar>(R.id.toolbar)
    }


    private val drawerLayout: DrawerLayout by lazy{
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    private val navView by lazy{
        findViewById<NavigationView>(R.id.navView)
    }

    private  val contentView: View by lazy{
        findViewById<CoordinatorLayout>(R.id.contentView)
    }

    private val context: Context by lazy {
        this
    }

    //user info
    fun getUserInfoData(): JSONObject? {

        if(!isLoggedIn()){
            startClassActivity(LoginActivity::class.java,true)
            return  null
        }

        val userInfo = secureSharedPref()
                .getJsonObject(USER_AUTH_INFO)

        if(userInfo == null){
            startClassActivity(LoginActivity::class.java,true)
            return null
        }

        return userInfo
    }//end user Info

    /**
     * userInfo
     */
    val userInfo by lazy {
        getUserInfoData()
    }


    private lateinit var drawerToggle: ActionBarDrawerToggle

    /**
     * check if user is logged in, since
     * this is almost inherited by most activities requiring login
     */
    override  fun onStart() {
        super.onStart()

       //lets get user info
        userInfo
    }//end on start

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        job = Job()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){

            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }


        //set toolbar as actionbar
        setSupportActionBar(toolbar)

        //disable actionbar title
        supportActionBar?.title = ""

        //remove elevation
        drawerLayout.drawerElevation = 0f


        //set Header Info
        setDrawerHeaderInfo()

        //set the scrim color (the dark overlay fading when the drawer is opened)
        drawerLayout.setScrimColor(Color.TRANSPARENT)

        var statusBarColor: Int? = null

        //drawer options and events
        drawerToggle = object: ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ){
            //events

            /**
             * onDrawerSlide
             */
            //push content on drawer open
            override fun onDrawerSlide(drawerView: View, offset: Float ){

                contentView.translationX = (offset * drawerView.width)

                //if
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                    if(statusBarColor == null){
                        statusBarColor = window.statusBarColor
                    }

                    //alpha
                    val alphaVal = (255 - ((offset / 1) * 255)).toInt()

                    val statusBarColorAlpha = statusBarColor!!.withAlpha(alphaVal)

                    window.statusBarColor = statusBarColorAlpha
                }//end if

                super.onDrawerSlide(drawerView, offset)
            }//end

            override fun onDrawerClosed(drawerView: View) {

                //if
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    if (statusBarColor != null) {
                         window.statusBarColor = statusBarColor!!
                    }

                    statusBarColor = null

                }//end

                super.onDrawerClosed(drawerView)
            }//end

        }//end toggle  options and events

        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(
                this,
                R.color.white
        )


        //lets set drawer listener
        drawerLayout.addDrawerListener(drawerToggle)


        //menu data
        val listData = mutableListOf(

                DrawerListModel(
                        "setting",
                        R.drawable.ic_settings_white_24dp,
                        getString(R.string.setting),
                        AboutActivity::class.java),

                DrawerListModel(
                        "support",
                        R.drawable.ic_email_24dp,
                        getString(R.string.support),
                        AboutActivity::class.java),

                DrawerListModel(
                        "about",
                        R.drawable.ic_info_black_24dp,
                        getString(R.string.about),
                        AboutActivity::class.java),

                DrawerListModel(
                        "logout",
                        R.drawable.ic_toys_black_24dp,
                        getString(R.string.logout),
                        AboutActivity::class.java)

        )//end list data

        //lets create populate menu list
        drawerListView.adapter = DrawerListAdapter(this,listData)


        /**
         * onItem Click
         */
        drawerListView.setOnItemClickListener { parent, view, position, id ->

            val tag = view.tag

            when(tag){

                "logout" -> {

                    AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(R.string.confirm_logout_message)
                            .setNegativeButton(R.string.no){ d,v ->
                                toast(R.string.logout_cancelled)
                                d.dismiss()
                            }
                            .setPositiveButton(R.string.yes){ d, v ->
                                Account(mActivity).doLogout()
                            }
                            .show()
                }//end if logout

            }//end when
        }//end listener

    }//end oncreate


    /**
     * ondestry cancel jobs
     */
    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
    }


    //onOptionItemSelected
    override fun onOptionsItemSelected(item: MenuItem):Boolean{

        when(item.itemId){
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }

        //if item
        if(drawerToggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }//end


    /**
     * onPostCreate
     */
    override fun onPostCreate(savedInstanceState: Bundle?){
        super.onPostCreate(savedInstanceState)

        //sync state of toggle
        drawerToggle.syncState()
    }//end


    /**
     * onconfiguration change tool.. we should
     * change the toggle
     */
    override fun onConfigurationChanged(newConfig: Configuration){
        super.onConfigurationChanged(newConfig)

        //onConfigurationChanged
        drawerToggle.onConfigurationChanged(newConfig)
    }//end

    /**
     * onCreatOptionsMenu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    //set Header info
    private fun setDrawerHeaderInfo(){

        val userEmail = userInfo!!.optString("user_email","")

        val userFullName = userInfo!!.optString("user_full_name","")

        //set name
        nameView.text = getString(R.string.hello_greeting,userFullName)

        emailOrPhoneView.text = "($userEmail)".toLowerCase()

        //lets get profile phot
        var photoUrl = userInfo!!.optString("photo_url",null)

        if(photoUrl == null || photoUrl.isEmpty()){
            photoUrl = getGravatar(userEmail)
        }

        //using glide insert pic
        Picasso
            .with(this)
            .load(photoUrl)
            .centerInside()
            .placeholder(R.drawable.ic_user_default)
            .error(R.drawable.ic_user_default)
            .resize(
                   photoViewParent.layoutParams.width,
                    photoViewParent.layoutParams.height
            )
            .into(photoView,object: Callback{

                        //cnvert to circular view
                  override fun onSuccess() {
                        val imageBitmap = photoView.drawable.toBitmap()
                        val imageDrawable = RoundedBitmapDrawableFactory.create(resources,imageBitmap)
                            imageDrawable.isCircular = true
                            imageDrawable.cornerRadius = (Math.max(imageBitmap.width,imageBitmap.height) / 2.0f)
                            photoView.setImageDrawable(imageDrawable)
                  }


                 override fun onError() {}
             })




    }//end set header info


}
